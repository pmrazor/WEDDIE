package com.ssafy.exhi.domain.couple.converter;

import com.ssafy.exhi.domain.couple.model.dto.CoupleRequest;
import com.ssafy.exhi.domain.couple.model.dto.CoupleResponse;
import com.ssafy.exhi.domain.couple.model.entity.Couple;
import com.ssafy.exhi.domain.notification.model.entity.CoupleMatchingNotification;
import com.ssafy.exhi.domain.user.converter.UserConverter;
import com.ssafy.exhi.domain.user.model.dto.UserResponse;
import com.ssafy.exhi.domain.user.model.entity.Gender;
import com.ssafy.exhi.domain.user.model.entity.User;

public class CoupleConverter {

    public static CoupleResponse.SimpleResultDTO toSimpleDTO(Couple couple) {
        UserResponse.SimpleResultDTO female = UserConverter.toSimpleResultDTO(couple.getFemale());
        UserResponse.SimpleResultDTO male = UserConverter.toSimpleResultDTO(couple.getMale());

        return CoupleResponse.SimpleResultDTO.builder()
                .id(couple.getId())
                .coupleName(couple.getCoupleName())
                .male(male)
                .female(female)
                .build();
    }

    public static CoupleResponse.DetailResultDTO toDetailDTO(Couple couple) {
        UserResponse.DetailResultDTO female = UserConverter.toDetailResultDTO(couple.getFemale());
        UserResponse.DetailResultDTO male = UserConverter.toDetailResultDTO(couple.getMale());

        return CoupleResponse.DetailResultDTO.builder()
                .id(couple.getId())
                .coupleName(couple.getCoupleName())
                .male(male)
                .female(female)
                .budget(couple.getBudget())
                .loveAnniversary(couple.getLoveAnniversary())
                .marriageDate(couple.getMarriageDate())
                .build();
    }

    public static Couple toEntity(CoupleRequest.CreateDTO request, User user1, User user2) {
        return Couple.builder()
                .coupleName(request.getCoupleName())
                .female(getFemaleUser(user1, user2))
                .male(getMaleUser(user1, user2))
                .build();
    }

    public static Couple toEntity(CoupleMatchingNotification notification) {
        User sender = notification.getSender();
        User receiver = notification.getReceiver();

        return Couple.builder()
                .coupleName(notification.getMessage())
                .male(getMaleUser(sender, receiver))
                .female(getFemaleUser(sender, receiver))
                .build();
    }

    private static User getFemaleUser(User user1, User user2) {
        return user1.getUserDetail().getGender() == Gender.FEMALE ? user1 : user2;
    }

    private static User getMaleUser(User user1, User user2) {
        return user1.getUserDetail().getGender() == Gender.MALE ? user1 : user2;
    }
}
