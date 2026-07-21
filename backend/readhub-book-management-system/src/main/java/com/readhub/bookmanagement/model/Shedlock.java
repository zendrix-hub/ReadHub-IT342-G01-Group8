package com.readhub.bookmanagement.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "shedlock")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shedlock {

    @Id
    @Column(name = "name", length = 64, nullable = false)
    private String name;

    @Column(name = "lock_until", nullable = false, columnDefinition = "TIMESTAMP(3)")
    private LocalDateTime lockUntil;

    @Column(name = "locked_at", nullable = false, columnDefinition = "TIMESTAMP(3)")
    private LocalDateTime lockedAt;

    @Column(name = "locked_by", nullable = false, length = 255)
    private String lockedBy;
}
