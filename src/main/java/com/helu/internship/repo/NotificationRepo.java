package com.helu.internship.repo;

import com.helu.internship.entity.NotificationEntity;
import com.helu.internship.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepo extends JpaRepository<NotificationEntity, UUID> {

    List<NotificationEntity> findByRecipientOrderByCreatedAtDesc(UserEntity recipient);

    long countByRecipientAndIsReadFalse(UserEntity recipient);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isRead = true WHERE n.id = :id")
    void markAsRead(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isRead = true WHERE n.recipient = :recipient")
    void markAllAsRead(@Param("recipient") UserEntity recipient);
}
