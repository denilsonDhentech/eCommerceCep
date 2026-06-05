package com.dhentech.eCommerceCep.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_log_consulta")
public class LogConsulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cep_consultado", nullable = false, length = 9)
    private String cepConsultado;

    @Column(name = "payload_retorno", columnDefinition = "TEXT")
    private String payloadRetorno;

    @Column(name = "data_hora_consulta", nullable = false)
    private LocalDateTime dataHoraConsulta;

    public LogConsulta() {
    }

    public LogConsulta(String cepConsultado, String payloadRetorno, LocalDateTime dataHoraConsulta) {
        this.cepConsultado = cepConsultado;
        this.payloadRetorno = payloadRetorno;
        this.dataHoraConsulta = dataHoraConsulta;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCepConsultado() { return cepConsultado; }
    public void setCepConsultado(String cepConsultado) { this.cepConsultado = cepConsultado; }

    public String getPayloadRetorno() { return payloadRetorno; }
    public void setPayloadRetorno(String payloadRetorno) { this.payloadRetorno = payloadRetorno; }

    public LocalDateTime getDataHoraConsulta() { return dataHoraConsulta; }
    public void setDataHoraConsulta(LocalDateTime dataHoraConsulta) { this.dataHoraConsulta = dataHoraConsulta; }
}