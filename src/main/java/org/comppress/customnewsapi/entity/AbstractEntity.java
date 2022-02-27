package org.comppress.customnewsapi.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@MappedSuperclass
@Data
public abstract class AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;
    @Version
    protected Long version;
    //@Convert(converter = LocalDateTimeConverter.class)
    protected LocalDateTime dateCreated = LocalDateTime.now();
    //@Convert(converter = LocalDateTimeConverter.class)
    protected LocalDateTime dateModified = LocalDateTime.now();

    @Column(columnDefinition = "TINYINT(1) DEFAULT 0")
    protected Boolean deleted;

    @PreUpdate
    protected void onUpdate(){
        this.dateModified = LocalDateTime.now();
    }

}
