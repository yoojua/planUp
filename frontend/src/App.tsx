import { useState, useEffect } from 'react'
import './App.css'

function App() {
    const [message, setMessage] = useState('')

    useEffect(() => {
        // 백엔드 API 호출 (/hello)
        fetch('http://localhost:8080/hello')
            .then(response => response.text()) // 데이터를 텍스트로 변환
            .then(data => setMessage(data))    // 받아온 데이터를 상태(message)에 저장
            .catch(error => console.error('Error:', error));
    }, []) // 빈 배열([]): 페이지가 처음 뜰 때 한 번만 실행

    return (
        <>
            <h1>프론트엔드 - 백엔드 연동 테스트</h1>
            <div className="card">
                <p>
                    백엔드에서 온 메시지: <br />
                    <strong>{message}</strong>
                </p>
            </div>
        </>
    )
}

export default App