package com.example.medhub;

import com.example.medhub.entity.Patient;
import com.example.medhub.repository.PatientRepository;
import com.example.medhub.service.CryptoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static com.example.medhub.enums.Authority.ROLE_PATIENT;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "medhub.security.encryption.key=12345678901234567890123456789012",
        "medhub.security.hashing.salt=somesalt"
})
class EncryptionIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    void shouldEncryptPeselAndAutomaticallyGenerateHash() {
        // Given
        String plaintextPesel = "90010112345";
        Patient patient = createBasePatient("patient.enc@test.com", plaintextPesel);

        // When
        patientRepository.save(patient);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Patient> retrieved = patientRepository.findByEmail("patient.enc@test.com");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getPesel()).isEqualTo(plaintextPesel);
        assertThat(retrieved.get().getPeselHash()).isNotNull();

        Object[] rawData = (Object[]) entityManager
                .createNativeQuery("SELECT pesel_encrypted, pesel_hash FROM patients WHERE user_id = :id")
                .setParameter("id", retrieved.get().getUserId())
                .getSingleResult();

        String encryptedInDb = (String) rawData[0];
        String hashInDb = (String) rawData[1];

        assertThat(encryptedInDb).isNotEqualTo(plaintextPesel);
        assertThat(hashInDb).isEqualTo(cryptoService.hash(plaintextPesel));
    }

    @Test
    @Transactional
    void shouldFailWhenSavingDuplicatePesel() {
        // Given
        String commonPesel = "90010112345";
        Patient p1 = createBasePatient("p1@test.com", commonPesel);
        patientRepository.save(p1);
        entityManager.flush();

        // When & Then
        Patient p2 = createBasePatient("p2@test.com", commonPesel);
        
        // We expect DataIntegrityViolationException because of the unique index on pesel_hash
        assertThrows(DataIntegrityViolationException.class, () -> {
            patientRepository.save(p2);
            entityManager.flush();
        });
    }

    @Test
    @Transactional
    void shouldAllowSamePeselAfterSoftDelete() {
        String commonPesel = "90010112345";
        Patient p1 = createBasePatient("p1@test.com", commonPesel);

        patientRepository.save(p1);
        patientRepository.delete(p1);

        entityManager.flush();
        entityManager.clear();

        Boolean isDeletedInDb = (Boolean) entityManager
                .createNativeQuery("SELECT deleted FROM patients WHERE user_id = :id")
                .setParameter("id", p1.getUserId())
                .getSingleResult();

        assertThat(isDeletedInDb).isTrue();

        Patient p2 = createBasePatient("p2@test.com",commonPesel);

        patientRepository.save(p2);
    }

    private Patient createBasePatient(String email, String pesel) {
        Patient patient = new Patient();
        patient.setEmail(email);
        patient.setPassword("pass");
        patient.setName("John");
        patient.setSurname("Doe");
        patient.setPhoneNumber("111222333");
        patient.setAuthority(ROLE_PATIENT);
        patient.setPesel(pesel);
        return patient;
    }

}
