package io.fundrequest.platform.profile.ref.domain;

import io.fundrequest.db.infrastructure.AbstractEntity;
import lombok.Builder;
import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "referral")
@Getter
public class Referral extends AbstractEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "referrer")
    private String referrer;

    @Column(name = "referee")
    private String referee;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ReferralStatus status;

    Referral() {
    }

    @Builder
    public Referral(String referrer, String referee, ReferralStatus status) {
        this.referrer = referrer;
        this.referee = referee;
        this.status = status;
    }

    public void setStatus(ReferralStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Referral referral = (Referral) o;
        return Objects.equals(referrer, referral.referrer) &&
               Objects.equals(referee, referral.referee);
    }

    @Override
    public int hashCode() {

        return Objects.hash(referrer, referee);
    }
}
