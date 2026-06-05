package com.dhentech.ecommercezipcode.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_log_consulta")
public class ConsultationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cep_consultado", nullable = false, length = 9)
    private String zipcodeQueryed;

    @Column(name = "payload_retorno", columnDefinition = "TEXT")
    private String payloadReturn;

    @Column(name = "data_hora_consulta", nullable = false)
    private LocalDateTime dateTimeConsultation;

    public ConsultationLog() {
    }

    public ConsultationLog(String zipcodeQueryed, String payloadReturn, LocalDateTime dateTimeConsultation) {
        this.zipcodeQueryed = zipcodeQueryed;
        this.payloadReturn = payloadReturn;
        this.dateTimeConsultation = dateTimeConsultation;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getZipcodeQueryed() { return zipcodeQueryed; }
    public void setZipcodeQueryed(String zipcodeQueryed) { this.zipcodeQueryed = zipcodeQueryed; }

    public String getPayloadReturn() { return payloadReturn; }
    public void setPayloadReturn(String payloadReturn) { this.payloadReturn = payloadReturn; }

    public LocalDateTime getDateTimeConsultation() { return dateTimeConsultation; }
    public void setDateTimeConsultation(LocalDateTime dateTimeConsultation) { this.dateTimeConsultation = dateTimeConsultation; }
}