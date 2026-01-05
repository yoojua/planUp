package domain.user;

import domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email; // 로그인 아이디

    @Column
    private String password; // 비밀번호 (소셜 로그인은 null 가능)

    @Column(nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING) // DB에 저장할 때 문자열("USER")로 저장
    @Column(nullable = false)
    private Role role;

    @Column
    private String socialType; // KAKAO, NAVER, NONE

    @Column
    private String socialId; // 소셜 로그인 식별값

    @Builder
    public User(String email, String password, String nickname, Role role, String socialType, String socialId) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
        this.socialType = socialType;
        this.socialId = socialId;
    }

    // 회원정보 수정 등의 비즈니스 로직도 여기에 추가 예정
    public String getRoleKey() {
        return this.role.getKey();
    }

    public User update(String nickname, String socialId) {
        this.nickname = nickname;
        this.socialId = socialId;
        return this;
    }
}
