# FireBasePhoneAuth
FireBasePhoneAuth  ( 파이어베이스 문자인증)

------

### package com.devhyeon.phoneauth.define

#### 위 패키지 위치에 API_KEY.kt 파일을 생성해주세요.

#### FIREBASE_AUTH_NONCE 를 작성해주세요.
val FIREBASE_AUTH_NONCE : ByteArray = "here" 

#### FIREBASE_AUTH_KEY 를 작성해주세요.
const val FIREBASE_AUTH_KEY : String = "here"

------

#### 1. 전화번호를 입력합니다.

#### 2. 전송을 누르면, 테스트 디바이스가 아닌경우 문자가전송되며, (가상기기가 아닌 Google Play Service 가 설치된 실기기라면, reCAPTCHA 과정 생략 ) 테스트 기기는 미전송됩니다.

#### 3. 전달받은 문자의 인증번호 or 테스트번호를 입력하면 인증이 가능합니다.

#### 4. 재전송도 가능합니다.

<img src="https://user-images.githubusercontent.com/72678200/115906869-1393a600-a4a3-11eb-86a1-c6d21f18a3cd.png" width="30%" height="30%"> <img src="https://user-images.githubusercontent.com/72678200/115906891-18f0f080-a4a3-11eb-9de4-ffcf9711e2a3.png" width="30%" height="30%"> <img src="https://user-images.githubusercontent.com/72678200/115906911-1f7f6800-a4a3-11eb-9560-e6a90a61866b.png" width="30%" height="30%">
<img src="https://user-images.githubusercontent.com/72678200/115906929-273f0c80-a4a3-11eb-8918-c03a8a35816e.png" width="30%" height="30%"> <img src="https://user-images.githubusercontent.com/72678200/115906945-2dcd8400-a4a3-11eb-836a-d22fddf4e6f9.png" width="30%" height="30%">
