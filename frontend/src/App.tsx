import { Routes, Route } from "react-router-dom";
import OAuthCallback from "./pages/OAuthCallback";
import { useState, useEffect } from "react";

function App() {
    const [isLogin, setIsLogin] = useState(false);

    useEffect(() => {
        const token = localStorage.getItem("accessToken");
        if (token) {
            setIsLogin(true);
        }
    }, []);

    const handleLogout = () => {
        const token = localStorage.getItem("accessToken");

        // 서버에 로그아웃 요청 (비동기로 보내고 기다리지 않아도 됨)
        if (token) {
            fetch('http://localhost:8080/api/auth/logout', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}` // 헤더에 토큰 실어 보내기
                }
            }).catch(err => console.error(err));
        }

        // 클라이언트 정리
        localStorage.removeItem("accessToken");
        setIsLogin(false);
        alert("로그아웃 되었습니다.");
    };

    return (
        <Routes>
            {/* 메인 페이지 */}
            <Route
                path="/"
                element={
                    <div style={{
                        display: "flex",
                        flexDirection: "column",
                        alignItems: "center",
                        justifyContent: "center",
                        minHeight: "100vh",
                        width: "100%"
                    }}>

                        <h1>PlanUp 메인 페이지 🏠</h1>
                        {isLogin ? (
                            <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: "10px" }}>
                                <h3>🎉 로그인 상태입니다!</h3>
                                <button onClick={handleLogout} style={{ padding: "10px 20px", cursor: "pointer" }}>
                                    로그아웃
                                </button>
                            </div>
                        ) : (
                            <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: "20px" }}>
                                <h3>로그인이 필요합니다.</h3>
                                {/* 백엔드 로그인 URL로 이동 */}
                                <a href="http://localhost:8080/oauth2/authorization/kakao">
                                    <img
                                        src="https://k.kakaocdn.net/14/dn/btroDszwNrM/I6efHub1SM5mIVl6r0mnBJ/o.jpg"
                                        alt="카카오 로그인"
                                        width="200"
                                        style={{ cursor: "pointer" }}
                                    />
                                </a>

                                <a href="http://localhost:8080/oauth2/authorization/naver">
                                    <button style={{
                                        backgroundColor: "#03C75A",
                                        color: "white",
                                        border: "none",
                                        padding: "10px 20px",
                                        width: "200px",
                                        cursor: "pointer",
                                        fontWeight: "bold"
                                    }}>
                                        네이버 로그인
                                    </button>
                                </a>
                            </div>
                        )}
                    </div>
                }
            />

            <Route path="/oauth/callback" element={<OAuthCallback />} />
        </Routes>
    );
}

export default App;