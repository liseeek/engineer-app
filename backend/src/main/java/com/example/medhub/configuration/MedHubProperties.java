package com.example.medhub.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "medhub")
public class MedHubProperties {

    private DoctorSelfSignup doctorSelfSignup = new DoctorSelfSignup();
    private Appointments appointments = new Appointments();
    private Ai ai = new Ai();

    @Getter
    @Setter
    public static class Ai {
        private boolean enabled = true;
    }

    @Getter
    @Setter
    public static class DoctorSelfSignup {
        /**
         * When false, POST /v1/doctors/signup returns 403 (use on public deployments).
         */
        private boolean enabled = false;
    }

    @Getter
    @Setter
    public static class Appointments {
        /**
         * Max upcoming appointments (date not before today) per patient with ACTIVE or RESCHEDULED status.
         */
        private int maxUpcomingPerPatient = 5;
    }

}
