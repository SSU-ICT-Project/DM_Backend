package com.dm.dmbackend.domain.notification.factory;

import com.dm.dmbackend.domain.account.member.entity.Member;
import com.dm.dmbackend.domain.account.member.entity.MemberFollow;
import com.dm.dmbackend.domain.account.member.entity.MemberFollowReq;
import com.dm.dmbackend.domain.llm.digitalDetox.entity.DigitalDetox;
import com.dm.dmbackend.domain.notification.dto.NotificationPayload;
import com.dm.dmbackend.domain.notification.entity.Notification;
import com.dm.dmbackend.domain.schedule.scheduleMessage.dto.ScheduleMessageRedisDto;

public final class NotificationPayloadFactory {

    private NotificationPayloadFactory() {}

    // 팔로우 요청 알림: requester -> receiver
    public static NotificationPayload memberFollowRequest(Member requester, Member receiver, MemberFollowReq reqEntity) {
        String senderNickname = requester.getNickname();
        String senderProfileUrl = extractProfileUrl(requester);
        String content = String.format("%s님이 팔로우를 요청하였습니다.", senderNickname);

        return new NotificationPayload(
                requester.getId(),                 // senderId
                senderNickname,                    // senderNickname
                senderProfileUrl,                  // senderProfileUrl
                receiver.getId(),                  // receiverId
                reqEntity.getId(),                 // objectId (follow request id)
                content,                           // content
                Notification.TargetObject.FOLLOW   // targetObject
        );
    }

    // 팔로우 수락 알림: receiver(수락한 사람) -> requester(요청한 사람)
    public static NotificationPayload memberFollowAccepted(Member accepter, Member requester, MemberFollow followEntity) {
        String senderNickname = accepter.getNickname();
        String senderProfileUrl = extractProfileUrl(accepter);
        String content = String.format("%s님이 팔로우 요청을 수락하였습니다.", senderNickname);

        return new NotificationPayload(
                accepter.getId(),                  // senderId (수락한 사람)
                senderNickname,
                senderProfileUrl,
                requester.getId(),                 // receiverId (요청한 사람)
                followEntity.getId(),              // objectId (follow id)
                content,
                Notification.TargetObject.FOLLOW
        );
    }

    // 디지털 중독 치료 알림: member -> member
    public static NotificationPayload digitalDetoxCure(Member member, DigitalDetox digitalDetox) {
        String senderNickname = member.getNickname();
        String senderProfileUrl = extractProfileUrl(member);

        return new NotificationPayload(
                member.getId(),
                senderNickname,
                senderProfileUrl,
                member.getId(),
                digitalDetox.getId(),
                digitalDetox.getMessage(),
                Notification.TargetObject.CURE
        );
    }

    // 디지털 중독 동기부여 알림: member -> member
    public static NotificationPayload digitalDetoxMotivate(Member member, DigitalDetox digitalDetox) {
        String senderNickname = member.getNickname();
        String senderProfileUrl = extractProfileUrl(member);

        return new NotificationPayload(
                member.getId(),
                senderNickname,
                senderProfileUrl,
                member.getId(),
                digitalDetox.getId(),
                digitalDetox.getMessage(),
                Notification.TargetObject.MOTIVATE
        );
    }

    // 일정 알림: member -> member
    public static NotificationPayload scheduleMessage(Member member, ScheduleMessageRedisDto dto) {
        String senderNickname = member.getNickname();
        String senderProfileUrl = extractProfileUrl(member);

        return new NotificationPayload(
                member.getId(),
                senderNickname,
                senderProfileUrl,
                member.getId(),
                dto.getId(),
                dto.getMessage(),
                Notification.TargetObject.SCHEDULE
        );
    }

    private static String extractProfileUrl(Member inviter) {
        try {
            return inviter.getProfileImageUrl();
        } catch (Exception ignored) {
            return null;
        }
    }
}
