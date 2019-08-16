# boot-RESTAPI
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



1. 서버 Test
2. 데이터베이스 실습(H2)
3. Swagger를 통한 API 문서 자동화 관리
4. REST API 구조 설계
5. Exception 처리