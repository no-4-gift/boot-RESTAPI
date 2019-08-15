# boot-RESTAPI
<br>

<br>

## Spring Boot Rest Api 실습

> Intellij Community로 진행

Create New Project → Gradle(Java)

```
GroupId : com.rest
ArtifactId : api
```

- Use auto-import, using qualified names 선택

build.gradle에 라이브러리 설정

```json
plugins {
    id 'org.springframework.boot' version '2.1.4.RELEASE'
    id 'java'
}

apply plugin: 'io.spring.dependency-management'

group 'com.rest'
version = '0.0.1-SNAPSHOT'

sourceCompatibility = 1.8

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-freemarker'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    compileOnly 'org.projectlombok:lombok'
    runtimeOnly 'com.h2database:h2'
    runtimeOnly 'mysql:mysql-connector-java'
    annotationProcessor 'org.projectlombok:lombok'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

File – Settings – Build, Execution, Deployment – Compiler – Annotation Processors 선택후 오른쪽 상단의 Enable annotation processing을 체크

> Lombok 어노테이션을 소스에서 인식시키기 위함

<br>

<br>

### 서버 실행해보기

---

`src/main/java/com/rest/api/Application.java`

```java
package com.rest.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Applications {
    public static void main(String[] args) {
        SpringApplication.run(Applications.class, args);
    }
}
```

<br>

resource 폴더에 환경설정 값을 저장할 `application.yml` 만들기

```yml
server:
  port: 8000
```

`com.rest.api`에 controller 패키지 만들고 HelloController.java 생성

```java
package com.rest.api.controller;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloController {

    @GetMapping(value = "/helloworld/string")
    @ResponseBody
    public String helloworldString() {
        return "helloworld";
    }

    @GetMapping(value = "/helloworld/json")
    @ResponseBody
    public Hello helloworldJson() {
        Hello hello = new Hello();
        hello.message = "helloworld";
        return hello;
    }

    @GetMapping(value = "/helloworld/page")
    public String helloworld() {
        return "helloworld";
    }

    @Setter
    @Getter
    public static class Hello {
        private String message;
    }
}
```

- @Controller : 해당 클래스가 컨트롤러임을 명시
- @GetMapping("RequestURI") : 주소의 Resource를 이용해 Get Method 호출해야 함
- @ResponseBody : 결과를 응답에 그대로 출력한다는 의미

<br>

**서버 실행** : Application.java 오른쪽 마우스 클릭 후, Run 실행하기

```
GetMapping으로 지정한 곳으로 들어가면, string과 json은 잘 출력되지만
page는 Whitelabel Error Page가 발생. 이유는 ResponseBody를 지정하지 않았기 때문
```

build.gradle에서 설정해둔 freemarker를 활용해 page template을 만들면 해결 가능함

`resources/templates`를 만들고 helloworld.ftl 파일 생성 후 내용은 HelloWorld 입력하면 page 매핑도 잘 출력되는 것 확인 가능

<br>

<br>

### 데이터베이스 실습

---

H2 Database 사용해보기 (경량 DB로 서버 요구사항이 낮고 MySQL처럼 다운로드할 필요없이 jar 파일만 실행시키면 되므로 간편하다고 함)

설치 후, bin/h2.bat 실행하기

<img src="https://daddyprogrammer.org/wp-content/uploads/2019/04/h2prop.jpg">

Generic H2(Server) 선택하기

<br>

application.yml에 접속 정보 추가

```yml
spring:
  datasource:
    url: jdbc:h2:tcp://localhost/~/test
    driver-class-name: org.h2.Driver
    username: sa
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    properties.hibernate.hbm2ddl.auto: update
    showSql: true
```

showSql : jpa가 실행하는 쿼리를 로그로 출력하기 위함

<br>

#### 테이블 매핑을 위한 모델 객체 만들어보기

com.rest.api에 entity 패키지 만들고 User 클래스 생성하기

```java
package com.rest.api.entity;

import lombok.*;
import javax.persistence.*;

@Builder
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long msrl;

    @Column(nullable = false, unique = true, length = 30)
    private String uid;

    @Column(nullable = false, length = 100)
    private String name;
}
```

- @Builder : builder 사용하기 위함
- @Entity : jpa 엔티티임을 알려줌
- @Getter : user 필드값의 getter 자동 생성(lombok)
- @NoArgsConstructor : 인자 없는 생성자 자동 생성
- @AllArgsConstructor : 인자를 모두 갖춘 생성자를 자동 생성
- @Table(name = "user") : user 테이블과 매핑됨을 명시해줌
- @Id : 기본키
- @GeneratedValue(strategy = GenerationType.IDENTITY) : pk를 auto_increment로 설정한 것

<br>

#### Repository 생성

생성한 User 클래스를 이용해 테이블에 질의 요청을 하기 위한 레파지토리를 생성하자

com.rest.api에 repo 패키지 만들고 UserJpaRepo 인터페이스 생성

```java
package com.rest.api.repo;

import com.rest.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepo extends JpaRepository<User, Integer> {}
```

<br>

#### UserController 생성

레파지토리를 사용하는 컨트롤러 하나를 생성한다. 버전관리를 위해 v1 패키지를 만들어 UserController를 생성하자

```java
package com.rest.api.controller.v1;

import com.rest.api.entity.User;
import com.rest.api.repo.UserJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1")
public class UserController {
    private final UserJpaRepo userJpaRepo;

    @GetMapping(value = "/user")
    public List<User> findAllUser() {
        return userJpaRepo.findAll();
    }

    @PostMapping(value = "/user")
    public User save() {
        User user = User.builder()
                .uid("gyuseok@gmail.com")
                .name("규석")
                .build();
        return userJpaRepo.save(user);
    }
}
```

- @RequiredArgsConstructor : final로 선언된 객체에 대해 Constructor Injection 수행함
- @RestController : 결과 데이터를 JSON으로 보냄
- @RequestMapping(value = "/v1") : api 리소스를 버전별 관리하기 위함
- @GetMapping(value = "/user") : user 테이블 데이터 모두 읽어옴 (List에 저장)
- @PostMapping(value = "/user") : user 테이블에 저장시키기

<br>

`http://localhost:8000/v1/user`로 들어가면, 아직 데이터가 없으므로 [] 로 빈 배열 출력됨

postman으로 post 해보면 결과가 달라지는 걸 확인할 수 있음

```
[{"msrl":1,"uid":"gyuseok@gmail.com","name":"규석"}]
```

<br>

***테이블 자동생성 막으려면?***

> 보안상 위험할 수 있음. jpa 설정에서 아래와 같이 지정

```
properties.hibernate.hbm2ddl.auto: none
```

