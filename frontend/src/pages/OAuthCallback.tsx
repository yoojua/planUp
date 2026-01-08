import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";

const OAuthCallback = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    useEffect(() => {
        // 1. URL에서 'token' 파라미터 추출
        const token = searchParams.get("token");

        if (token) {
            console.log("로그인 성공! 토큰:", token);

            // 2. 로컬 스토리지에 토큰 저장 (브라우저 껐다 켜도 유지)
            localStorage.setItem("accessToken", token);

            // 3. 메인 페이지로 이동 (새로고침 효과를 위해 window.location 사용하거나 navigate 사용)
            navigate("/");
        } else {
            console.error("토큰이 없습니다. 로그인 실패");
            navigate("/login");
        }
    }, [searchParams, navigate]);

    return <div>로그인 처리 중입니다... 잠시만 기다려주세요. </div>;
};

export default OAuthCallback;