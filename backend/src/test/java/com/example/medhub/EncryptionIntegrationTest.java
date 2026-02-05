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
    void shouldEncryptPeselAndAllowRetrieval() {
        String plaintextPesel = "90010112345";
        Patient patient = new Patient();
        patient.setEmail("patient.enc@test.com");
        patient.setPassword("pass");
        patient.setName("John");
        patient.setSurname("Doe");
        patient.setPhoneNumber("111222333");
        patient.setAuthority(ROLE_PATIENT);

        assertThat(patient.getPhoneNumber()).isEqualTo("111222333");
        assertThat(patient.getName()).isEqualTo("John");

        patient.setPesel(plaintextPesel);
        patient.setPeselHash(cryptoService.hash(plaintextPesel));

        patientRepository.save(patient);
        entityManager.flush();
        entityManager.clear();

        Optional<Patient> retrieved = patientRepository.findByEmail("patient.enc@test.com");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getPesel()).isEqualTo(plaintextPesel);

        Object rawEncrypted = entityManager
                .createNativeQuery("SELECT pesel_encrypted FROM patients WHERE user_id = :id")
                .setParameter("id", retrieved.get().getUserId())
                .getSingleResult();

        assertThat(rawEncrypted.toString()).isNotEqualTo(plaintextPesel);
        assertThat(rawEncrypted.toString().length()).isGreaterThan(20);
    }

}
