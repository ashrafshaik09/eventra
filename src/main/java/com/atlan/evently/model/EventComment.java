package com.atlan.evently.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity for user comments on events with support for nested replies.
 */
@Entity
@Table(name = "event_comments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventComment {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "comment_text", nullable = false, columnDefinition = "TEXT")
    private String commentText;

    // Self-referencing for nested comments/replies
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private EventComment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EventComment> replies = new ArrayList<>();

    @Column(name = "is_edited", nullable = false)
    @Builder.Default
    private Boolean isEdited = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private ZonedDateTime createdAt = ZonedDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private ZonedDateTime updatedAt = ZonedDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = ZonedDateTime.now();
        this.isEdited = true;
    }

    // Helper methods
    public boolean isReply() {
        return parentComment != null;
    }

    public boolean hasReplies() {
        return replies != null && !replies.isEmpty();
    }

    public int getReplyCount() {
        return replies != null ? replies.size() : 0;
    }
}
