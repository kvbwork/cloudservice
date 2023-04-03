package ru.netology.cloudservice.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "files_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity owner;

    private String filename;

    private long filesize;

    private String hash;

    private String contentUid;

    private Instant createdAt = Instant.now();

    private Instant deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void setDeleted(boolean value) {
        deletedAt = value ? Instant.now() : null;
    }

}
