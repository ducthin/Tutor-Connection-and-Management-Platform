package com.upsilon.TCMP.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Messages", indexes = {
    @Index(name = "idx_conversation", columnList = "sender_id, receiver_id, sent_at")
})
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(nullable = false)
    private String content;

    @Column(name = "sent_at")
    private LocalDateTime sentAt = LocalDateTime.now();

    @Column(name = "is_read")
    private Boolean isRead = false;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private Session session;

    @Column(name = "is_archived")
    private Boolean archived = false;

    public Boolean getArchived() {
        return archived;
    }


    public void setArchived(Boolean archived) {
        this.archived = archived;
    }
}