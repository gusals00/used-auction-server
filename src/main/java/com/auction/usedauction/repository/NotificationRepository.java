package com.auction.usedauction.repository;

import com.auction.usedauction.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByMember_LoginIdAndChecked(String loginId, boolean checked);

    Long countByMember_LoginIdAndChecked(String loginId, boolean checked);
}
