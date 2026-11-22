package carservice.domain;

import carservice.domain.enumeration.JobStatus;
import carservice.domain.enumeration.MechanicSpecialization;
import carservice.exception.DomainException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class JobTest {

    @Test
    void shouldCreateJobSuccessfully() {
        Job job = new Job("Test Repair", "Engine repair", new BigDecimal("1000"), new BigDecimal("500"), MechanicSpecialization.ENGINE);

        assertEquals("Test Repair", job.getName());
        assertEquals("Engine repair", job.getDescription());
        assertEquals(new BigDecimal("1000"), job.getPartsCost());
        assertEquals(new BigDecimal("500"), job.getLaborCost());
        assertEquals(MechanicSpecialization.ENGINE, job.getRequiredSpecialization());
        assertEquals(JobStatus.PENDING, job.getStatus());
        assertNull(job.getRepairOrder());
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        assertThrows(DomainException.class, () -> new Job("", "Engine repair", new BigDecimal("1000"), new BigDecimal("500"), MechanicSpecialization.ENGINE));
        assertThrows(DomainException.class, () -> new Job(null, "Engine repair", new BigDecimal("1000"), new BigDecimal("500"), MechanicSpecialization.ENGINE));
    }

    @Test
    void shouldThrowExceptionWhenDescriptionIsBlank() {
        assertThrows(DomainException.class, () -> new Job("Test Repair", "", new BigDecimal("1000"), new BigDecimal("500"), MechanicSpecialization.ENGINE));
        assertThrows(DomainException.class, () -> new Job("Test Repair", null, new BigDecimal("1000"), new BigDecimal("500"), MechanicSpecialization.ENGINE));
    }

    @Test
    void shouldThrowExceptionWhenPartsCostIsInvalid() {
        assertThrows(DomainException.class, () -> new Job("Test Repair", "Engine repair", null, new BigDecimal("500"), MechanicSpecialization.ENGINE));
        assertThrows(DomainException.class, () -> new Job("Test Repair", "Engine repair", new BigDecimal("-1"), new BigDecimal("500"), MechanicSpecialization.ENGINE));
    }

    @Test
    void shouldCreateJobWithZeroPartsCost() {
        Job job = new Job("Test Repair", "Engine repair", BigDecimal.ZERO, new BigDecimal("500"), MechanicSpecialization.ENGINE);
        assertEquals(0, BigDecimal.ZERO.compareTo(job.getPartsCost()));
    }

    @Test
    void shouldThrowExceptionWhenLaborCostIsInvalid() {
        assertThrows(DomainException.class, () -> new Job("Test Repair", "Engine repair", new BigDecimal("1000"), null, MechanicSpecialization.ENGINE));
        assertThrows(DomainException.class, () -> new Job("Test Repair", "Engine repair", new BigDecimal("1000"), new BigDecimal("-10"), MechanicSpecialization.ENGINE));
    }

    @Test
    void shouldCreateJobWithZeroLaborCost() {
        Job job = new Job("Test Repair", "Engine repair", new BigDecimal("1000"), BigDecimal.ZERO, MechanicSpecialization.ENGINE);
        assertEquals(0, BigDecimal.ZERO.compareTo(job.getLaborCost()));
    }

    @Test
    void shouldThrowExceptionWhenSpecializationIsNull() {
        assertThrows(DomainException.class, () -> new Job("Test Repair", "Engine repair", new BigDecimal("1000"), new BigDecimal("500"), null));
    }

    @Test
    void shouldChangeStatusToInProgressWhenStarted() {
        Job job = new Job("Test Repair", "Engine repair", new BigDecimal("1000"), new BigDecimal("500"), MechanicSpecialization.ENGINE);
        job.start();

        assertEquals(JobStatus.IN_PROGRESS, job.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenStartingAlreadyStartedJob() {
        Job job = new Job("Test Repair", "Engine repair", new BigDecimal("1000"), new BigDecimal("500"), MechanicSpecialization.ENGINE);
        job.start();

        DomainException exception = assertThrows(DomainException.class, job::start);
        assertTrue(exception.getMessage().contains("PENDING status"));
    }

    @Test
    void shouldChangeStatusToCompletedWhenCompleteCalled() {
        Job job = new Job("Test Repair", "Engine repair", new BigDecimal("1000"), new BigDecimal("500"), MechanicSpecialization.ENGINE);
        job.start();
        job.complete();

        assertEquals(JobStatus.COMPLETED, job.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenCompletingPendingJob() {
        Job job = new Job("Test Repair", "Engine repair", new BigDecimal("1000"), new BigDecimal("500"), MechanicSpecialization.ENGINE);

        DomainException exception = assertThrows(DomainException.class, job::complete);
        assertTrue(exception.getMessage().contains("progress"));
    }

    @Test
    void shouldThrowExceptionWhenCompletingAlreadyCompletedJob() {
        Job job = new Job("Test Repair", "Engine repair", new BigDecimal("1000"), new BigDecimal("500"), MechanicSpecialization.ENGINE);
        job.start();
        job.complete();

        DomainException exception = assertThrows(DomainException.class, job::complete);
        assertTrue(exception.getMessage().contains("progress"));
    }

    @Test
    void shouldSetRepairOrderSuccessfully() {
        Job job = new Job("Test Repair", "Engine repair", new BigDecimal("1000"), new BigDecimal("500"), MechanicSpecialization.ENGINE);

        job.setRepairOrder(null);

        assertNull(job.getRepairOrder());
    }
}
