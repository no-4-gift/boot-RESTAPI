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

<br>

<br>

### Swagger를 통한 API 문서 자동화 관리

`build.gradle`에  swagger 라이브러리 추가

```
implementation 'io.springfox:springfox-swagger2:2.6.1'
implementation 'io.springfox:springfox-swagger-ui:2.6.1'
```

com.rest.api에 config 패키지 생성하고 SwaggerConfiguration 파일 만들기

```java
package com.rest.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {
    @Bean
    public Docket swaggerApi() {
        return new Docket(DocumentationType.SWAGGER_2).apiInfo(swaggerInfo()).select()
                .apis(RequestHandlerSelectors.basePackage("com.rest.api.controller"))
                .paths(PathSelectors.any())
                .build()
                .useDefaultResponseMessages(false); // 기본으로 세팅되는 200,401,403,404 메시지를 표시 하지 않음
    }

    private ApiInfo swaggerInfo() {
        return new ApiInfoBuilder().title("Spring API Documentation")
                .description("앱 개발시 사용되는 서버 API에 대한 연동 문서입니다")
                .license("kim6394").licenseUrl("https://github.com/no-4-gift").version("1").build();
    }
}
```

> 나중에 v1 버전만 문서화하고 싶으면 **PathSelectors.ant(“/ v1/\**”)** 하면 됨 

<br>

##### UserController 수정하기

```java
package com.rest.api.controller.v1;

import com.rest.api.entity.User;
import com.rest.api.repo.UserJpaRepo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = {"1. User"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1")
public class UserController {
    private final UserJpaRepo userJpaRepo;

    @ApiOperation(value = "회원 조회", notes = "모든 회원을 조회한다")
    @GetMapping(value = "/user")
    public List<User> findAllUser() {
        return userJpaRepo.findAll();
    }

    @ApiOperation(value = "회원 입력", notes = "회원을 입력한다.")
    @PostMapping(value = "/user")
    public User save(@ApiParam(value = "회원아이디", required = true) @RequestParam String uid,
                     @ApiParam(value = "회원이름", required = true) @RequestParam String name) {
        User user = User.builder()
                .uid(uid)
                .name(name)
                .build();
        return userJpaRepo.save(user);
    }
}
```

- @Api(tags = {“1. User”}) : UserController를 대표하는 최상단 타이틀 영역에 표시될 값
- @ApiOperation(value = "회원 조회", notes = "모든 회원을 조회한다") : 각각 resource에 제목과 설명 표시
- @ApiParam(value = "회원아이디", required = true) @RequestParam String uid : 파라미터 설명 표시

<br>

`http://localhost:8000/swagger-ui.html`로 접속하면 API 문서가 이쁘게 작성되어있는 모습을 확인할 수 있다.

<br>

<br>

### REST API 구조 설계하기

---

클라이언트에게 api를 제공해야 한다. 한번 배포하고 공유한 api는 구조를 쉽게 바꿀 수 없으므로 처음부터 효율적이고 확장 가능한 형태로 모델을 설계해야 한다.

HttpMethod를 사용해 RESTFul한 api를 만들 수 있도록 몇가지 규칙을 적용하자.

<br>

1. Resource 사용 목적에 따라 HTTP Method 구분해서 사용

   ```
   GET : 서버에 주어진 리소스 정보 요청 (읽기)
   POST : 서버에 리소스를 제출 (쓰기)
   PUT : 서버에 리소스를 제출. POST와 달리 리소스 갱신에 사용 (수정)
   DELETE : 서버에 주어진 리소스 삭제 요청 (삭제)
   ```

2. 매핑된 주소 체계를 정형화

   ```
   GET /v1/user/{userId} : 회원 userId에 해당하는 정보 조회
   GET /v1/users : 회원 리스트 조회
   POST /v1/user : 신규 회원정보 입력
   PUT /v1/user : 기존회원 정보 수정
   DELETE /v1/user/{userId} : userId로 기존회원 정보 삭제
   ```

3. 결과 데이터 구조 표준화해서 정의하기

   ```json
   // 기존 USER 정보
   {
       "msrl": 1,
       "uid": "gyuseok@gmail.com",
       "name": "규석"
   }
   // 표준화한 USER 정보
   {
     "data": {
       "msrl": 1,
       "uid": "gyuseok@gmail.com",
       "name": "규석"
     },
     "success": true
     "code": 0,
     "message": "성공하였습니다."
   }
   ```

<br>

데이터 구조를 설계할 결과 모델을 구현해보자

#### 결과를 담아낼 model 생성 

com.rest.api에 model.response 패키지를 생성한다. 이 패키지에 결과를 담을 3개 모델을 생성하자

1. ##### api 실행 결과를 담을 CommonResult

   ```java
   package com.rest.api.model.response;
   
   import io.swagger.annotations.ApiModelProperty;
   import lombok.*;
   
   @Getter
   @Setter
   public class CommonResult {
   
       @ApiModelProperty(value = "응답 성공여부 : true/false")
       private boolean success;
   
       @ApiModelProperty(value = "응답 코드 번호 : >= 0 정상, < 0 비정상")
       private int code;
   
       @ApiModelProperty(value = "응답 메시지")
       private String msg;
   }
   ```

   api의 처리 상태 및 메시지를 내려주는 데이터로 구성됨. 
   success는 api의 성공/실패 여부, code/msg는 응답 코드와 메시지를 나타냄
   <br>

2. ##### 결과가 단일 건인 api를 담는 SingleResult

   ```java
   package com.rest.api.model.response;
   
   import lombok.*;
   
   @Getter
   @Setter
   public class SingleResult<T> extends CommonResult {
       private T data;
   }
   ```

   CommonResult를 상속받으면서 api 요청 결과도 같이 출력되도록 함
   <br>

3. ##### 결과가 여러 건인 API를 담는 ListResult

   ```java
   package com.rest.api.model.response;
   
   import lombok.*;
   import java.util.List;
   
   @Getter
   @Setter
   public class ListResult<T> extends CommonResult {
       private List<T> list;
   }
   ```

   결과가 여러개일 경우는 List로 리턴하도록 구현했음
   <br>

<br>

#### 결과 모델을 처리할 서비스 생성

com.rest.api에 service 패키지를 생성하고 ResponseService 클래스 구현하기

```java
package com.rest.api.service;

import com.rest.api.model.response.CommonResult;
import com.rest.api.model.response.ListResult;
import com.rest.api.model.response.SingleResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResponseService {

    // enum으로 api 요청 결과에 대한 code, message를 정의합니다.
    public enum CommonResponse {
        SUCCESS(0, "성공하였습니디."),
        FAIL(-1, "실패하였습니다.");

        int code;
        String msg;

        CommonResponse(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
    // 단일건 결과를 처리하는 메소드
    public <T> SingleResult<T> getSingleResult(T data) {
        SingleResult<T> result = new SingleResult<>();
        result.setData(data);
        setSuccessResult(result);
        return result;
    }
    // 다중건 결과를 처리하는 메소드
    public <T> ListResult<T> getListResult(List<T> list) {
        ListResult<T> result = new ListResult<>();
        result.setList(list);
        setSuccessResult(result);
        return result;
    }
    // 성공 결과만 처리하는 메소드
    public CommonResult getSuccessResult() {
        CommonResult result = new CommonResult();
        setSuccessResult(result);
        return result;
    }
    // 실패 결과만 처리하는 메소드
    public CommonResult getFailResult() {
        CommonResult result = new CommonResult();
        result.setSuccess(false);
        result.setCode(CommonResponse.FAIL.getCode());
        result.setMsg(CommonResponse.FAIL.getMsg());
        return result;
    }
    // 결과 모델에 api 요청 성공 데이터를 세팅해주는 메소드
    private void setSuccessResult(CommonResult result) {
        result.setSuccess(true);
        result.setCode(CommonResponse.SUCCESS.getCode());
        result.setMsg(CommonResponse.SUCCESS.getMsg());
    }

}
```

<br>

이제 서비스 클래스 구현이 되었으므로, HttpMethod와 정형화된 주소체계를 가진 UserController를 만들어줘야한다.

즉, 기존 기능을 service를 타고 넘어오도록 수정하자

```java
package com.rest.api.controller.v1;

import com.rest.api.entity.User;
import com.rest.api.model.response.CommonResult;
import com.rest.api.model.response.ListResult;
import com.rest.api.model.response.SingleResult;
import com.rest.api.repo.UserJpaRepo;
import com.rest.api.service.ResponseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Api(tags = {"1. User"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1")
public class UserController {
    private final UserJpaRepo userJpaRepo;
    private final ResponseService responseService;

    @ApiOperation(value = "회원 리스트 조회", notes = "모든 회원을 조회한다")
    @GetMapping(value = "/users")
    public ListResult<User> findAllUser() {
        // 결과데이터가 여러건인경우 getListResult를 이용해서 결과를 출력한다.
        return responseService.getListResult(userJpaRepo.findAll());
    }

    @ApiOperation(value = "회원 단건 조회", notes = "userId로 회원을 조회한다")
    @GetMapping(value = "/user/{msrl}")
    public SingleResult<User> findUserById(@ApiParam(value = "회원ID", required = true) @PathVariable long msrl) {
        // 결과데이터가 단일건인경우 getBasicResult를 이용해서 결과를 출력한다.
        return responseService.getSingleResult(userJpaRepo.findById(msrl).orElse(null));
    }

    @ApiOperation(value = "회원 입력", notes = "회원을 입력한다.")
    @PostMapping(value = "/user")
    public SingleResult<User> save(@ApiParam(value = "회원아이디", required = true) @RequestParam String uid,
                     @ApiParam(value = "회원이름", required = true) @RequestParam String name) {
        User user = User.builder()
                .uid(uid)
                .name(name)
                .build();
        return responseService.getSingleResult(userJpaRepo.save(user));
    }

    @ApiOperation(value = "회원 수정", notes = "회원정보를 수정한다")
    @PutMapping(value = "/user")
    public SingleResult<User> modify(
            @ApiParam(value = "회원번호", required = true) @RequestParam long msrl,
            @ApiParam(value = "회원아이디", required = true) @RequestParam String uid,
            @ApiParam(value = "회원이름", required = true) @RequestParam String name) {
        User user = User.builder()
                .msrl(msrl)
                .uid(uid)
                .name(name)
                .build();
        return responseService.getSingleResult(userJpaRepo.save(user));
    }

    @ApiOperation(value = "회원 삭제", notes = "userId로 회원정보를 삭제한다")
    @DeleteMapping(value = "/user/{msrl}")
    public CommonResult delete(
            @ApiParam(value = "회원번호", required = true) @PathVariable long msrl) {
        userJpaRepo.deleteById(msrl);
        // 성공 결과 정보만 필요한경우 getSuccessResult()를 이용하여 결과를 출력한다.
        return responseService.getSuccessResult();
    }
}
```

> 기존의 repo 패키지의 UserJpaRepo에서 interface의 Integer를 Long으로 수정해줘야 msrl을 넣을 수 있다.

정형화된 Swagger 문서의 결과는 아래와 같다.

<img src="https://daddyprogrammer.org/wp-content/uploads/2019/04/swagger_model_1.png">



