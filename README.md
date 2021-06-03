<img src="https://user-images.githubusercontent.com/43338817/118908353-3eff9880-b95c-11eb-82f5-de2868e3ae4e.png" alt="" data-canonical-src="https://user-images.githubusercontent.com/43338817/118908353-3eff9880-b95c-11eb-82f5-de2868e3ae4e.png" width="250" height="250" /> <img src="https://user-images.githubusercontent.com/43338817/118910199-0c0ad400-b95f-11eb-8165-c469394fa8ab.png" alt="" data-canonical-src="https://user-images.githubusercontent.com/43338817/118910199-0c0ad400-b95f-11eb-8165-c469394fa8ab.png" width="250" height="250" />

# 예제 -  호텔예약서비스

- 체크포인트 : https://workflowy.com/s/assessment-check-po/T5YrzcMewfo4J6LW


# Table of contents

- [예제 - 호텔예약서비스](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [체크포인트](#체크포인트)
  - [분석/설계](#분석설계)
  - [구현:](#구현-)
    - [DDD 의 적용](#ddd-의-적용)
    - [폴리글랏 퍼시스턴스](#폴리글랏-퍼시스턴스)
    - [폴리글랏 프로그래밍](#폴리글랏-프로그래밍)
    - [동기식 호출 과 Fallback 처리](#동기식-호출-과-Fallback-처리)
    - [비동기식 호출 과 Eventual Consistency](#비동기식-호출-과-Eventual-Consistency)
  - [운영](#운영)
    - [CI/CD 설정](#cicd설정)
    - [동기식 호출 / 서킷 브레이킹 / 장애격리](#동기식-호출-서킷-브레이킹-장애격리)
    - [오토스케일 아웃](#오토스케일-아웃)
    - [무정지 재배포](#무정지-재배포)
    - [ConfigMap 사용](#ConfigMap-사용)
    - [Self-healing (Liveness Probe)](#Self-healing-Liveness-Probe)
    - [CQRS](#CQRS)
  - [신규 개발 조직의 추가](#신규-개발-조직의-추가)

# 서비스 시나리오

호텔 예약 서비스 따라하기

기능적 요구사항
1. 호스트가 룸을 등록한다.
1. 호스트가 룸을 삭제한다.
3. 게스트가 룸을 검색한다.
4. 게스트가 룸을 선택하여 사용 예약한다.
5. 게스트가 결제한다. (Sync, 결제서비스)
6. 결제가 완료되면, 결제 & 예약 내용을 게스트에게 알림을 전송한다. (Async, 알림서비스)
7. 예약 내역을 호스트에게 전달한다.
8. 게스트는 본인의 예약 내용 및 상태를 조회한다.
9. 게스트는 본인의 예약을 취소할 수 있다.
12. 예약이 취소되면, 결제를 취소한다. (Async, 결제서비스)
13. 결제가 취소되면, 결제 취소 내용을 게스트에게 알림을 전송한다. (Async, 알림서비스)


비기능적 요구사항
1. 트랜잭션
    1. 결제가 되지 않은 예약건은 아예 거래가 성립되지 않아야 한다 - Sync 호출 
1. 장애격리
    1. 통지(알림) 기능이 수행되지 않더라도 예약은 365일 24시간 받을 수 있어야 한다 - Async (event-driven), Eventual Consistency
    1. 결제시스템이 과중되면 사용자를 잠시동안 받지 않고 결제를 잠시후에 하도록 유도한다 - Circuit breaker, fallback
1. 성능
    1. 게스트와 호스트가 자주 예약관리에서 확인할 수 있는 상태를 마이페이지(프론트엔드)에서 확인할 수 있어야 한다 - CQRS
    1. 처리상태가 바뀔때마다 email, app push 등으로 알림을 줄 수 있어야 한다 - Event driven



# 체크포인트

- 분석 설계


  - 이벤트스토밍: 
    - 스티커 색상별 객체의 의미를 제대로 이해하여 헥사고날 아키텍처와의 연계 설계에 적절히 반영하고 있는가?
    - 각 도메인 이벤트가 의미있는 수준으로 정의되었는가?
    - 어그리게잇: Command와 Event 들을 ACID 트랜잭션 단위의 Aggregate 로 제대로 묶었는가?
    - 기능적 요구사항과 비기능적 요구사항을 누락 없이 반영하였는가?    

  - 서브 도메인, 바운디드 컨텍스트 분리
    - 팀별 KPI 와 관심사, 상이한 배포주기 등에 따른  Sub-domain 이나 Bounded Context 를 적절히 분리하였고 그 분리 기준의 합리성이 충분히 설명되는가?
      - 적어도 3개 이상 서비스 분리
    - 폴리글랏 설계: 각 마이크로 서비스들의 구현 목표와 기능 특성에 따른 각자의 기술 Stack 과 저장소 구조를 다양하게 채택하여 설계하였는가?
    - 서비스 시나리오 중 ACID 트랜잭션이 크리티컬한 Use 케이스에 대하여 무리하게 서비스가 과다하게 조밀히 분리되지 않았는가?
  - 컨텍스트 매핑 / 이벤트 드리븐 아키텍처 
    - 업무 중요성과  도메인간 서열을 구분할 수 있는가? (Core, Supporting, General Domain)
    - Request-Response 방식과 이벤트 드리븐 방식을 구분하여 설계할 수 있는가?
    - 장애격리: 서포팅 서비스를 제거 하여도 기존 서비스에 영향이 없도록 설계하였는가?
    - 신규 서비스를 추가 하였을때 기존 서비스의 데이터베이스에 영향이 없도록 설계(열려있는 아키택처)할 수 있는가?
    - 이벤트와 폴리시를 연결하기 위한 Correlation-key 연결을 제대로 설계하였는가?

  - 헥사고날 아키텍처
    - 설계 결과에 따른 헥사고날 아키텍처 다이어그램을 제대로 그렸는가?
    
- 구현
  - [DDD] 분석단계에서의 스티커별 색상과 헥사고날 아키텍처에 따라 구현체가 매핑되게 개발되었는가?
    - Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 데이터 접근 어댑터를 개발하였는가
    - [헥사고날 아키텍처] REST Inbound adaptor 이외에 gRPC 등의 Inbound Adaptor 를 추가함에 있어서 도메인 모델의 손상을 주지 않고 새로운 프로토콜에 기존 구현체를 적응시킬 수 있는가?
    - 분석단계에서의 유비쿼터스 랭귀지 (업무현장에서 쓰는 용어) 를 사용하여 소스코드가 서술되었는가?
  - Request-Response 방식의 서비스 중심 아키텍처 구현
    - 마이크로 서비스간 Request-Response 호출에 있어 대상 서비스를 어떠한 방식으로 찾아서 호출 하였는가? (Service Discovery, REST, FeignClient)
    - 서킷브레이커를 통하여  장애를 격리시킬 수 있는가?
  - 이벤트 드리븐 아키텍처의 구현
    - 카프카를 이용하여 PubSub 으로 하나 이상의 서비스가 연동되었는가?
    - Correlation-key:  각 이벤트 건 (메시지)가 어떠한 폴리시를 처리할때 어떤 건에 연결된 처리건인지를 구별하기 위한 Correlation-key 연결을 제대로 구현 하였는가?
    - Message Consumer 마이크로서비스가 장애상황에서 수신받지 못했던 기존 이벤트들을 다시 수신받아 처리하는가?
    - Scaling-out: Message Consumer 마이크로서비스의 Replica 를 추가했을때 중복없이 이벤트를 수신할 수 있는가
    - CQRS: Materialized View 를 구현하여, 타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 내 서비스의 화면 구성과 잦은 조회가 가능한가?

  - 폴리글랏 플로그래밍
    - 각 마이크로 서비스들이 하나이상의 각자의 기술 Stack 으로 구성되었는가?
    - 각 마이크로 서비스들이 각자의 저장소 구조를 자율적으로 채택하고 각자의 저장소 유형 (RDB, NoSQL, File System 등)을 선택하여 구현하였는가?
  - API 게이트웨이
    - API GW를 통하여 마이크로 서비스들의 집입점을 통일할 수 있는가?
    - 게이트웨이와 인증서버(OAuth), JWT 토큰 인증을 통하여 마이크로서비스들을 보호할 수 있는가?
- 운영
  - SLA 준수
    - 셀프힐링: Liveness Probe 를 통하여 어떠한 서비스의 health 상태가 지속적으로 저하됨에 따라 어떠한 임계치에서 pod 가 재생되는 것을 증명할 수 있는가?
    - 서킷브레이커, 레이트리밋 등을 통한 장애격리와 성능효율을 높힐 수 있는가?
    - 오토스케일러 (HPA) 를 설정하여 확장적 운영이 가능한가?
    - 모니터링, 앨럿팅: 
  - 무정지 운영 CI/CD (10)
    - Readiness Probe 의 설정과 Rolling update을 통하여 신규 버전이 완전히 서비스를 받을 수 있는 상태일때 신규버전의 서비스로 전환됨을 siege 등으로 증명 
    - Contract Test :  자동화된 경계 테스트를 통하여 구현 오류나 API 계약위반를 미리 차단 가능한가?


# 분석/설계


## AS-IS 조직 (Horizontally-Aligned)
  ![image](https://user-images.githubusercontent.com/487999/79684144-2a893200-826a-11ea-9a01-79927d3a0107.png)

## TO-BE 조직 (Vertically-Aligned)
  ![image](https://user-images.githubusercontent.com/487999/79684159-3543c700-826a-11ea-8d5f-a3fc0c4cad87.png)


## Event Storming 결과
* MSAEz 로 모델링한 이벤트스토밍 결과:  http://www.msaez.io/#/storming/Bb0WFdkafROy3qiBkH258FKxOLW2/share/8efe31263c3025004aaa3da86965bf52


### 이벤트 도출
![image](https://user-images.githubusercontent.com/45786659/118963296-42694300-b9a1-11eb-9e67-05148a338fab.png)

### 부적격 이벤트 탈락
![image](https://user-images.githubusercontent.com/45786659/118963312-46956080-b9a1-11eb-9600-f6868b1672f8.png)

    - 과정중 도출된 잘못된 도메인 이벤트들을 걸러내는 작업을 수행함
        - 룸검색됨, 예약정보조회됨 :  UI 의 이벤트이지, 업무적인 의미의 이벤트가 아니라서 제외

### 액터, 커맨드 부착하여 읽기 좋게
![image](https://user-images.githubusercontent.com/45786659/118963330-4bf2ab00-b9a1-11eb-81b2-f44fc65f1cca.png)

### 어그리게잇으로 묶기
![image](https://user-images.githubusercontent.com/45786659/118963371-59a83080-b9a1-11eb-9ade-4556d1fb4cc8.png)

    - 룸, 예약관리, 결제는 그와 연결된 command 와 event 들에 의하여 트랜잭션이 유지되어야 하는 단위로 그들 끼리 묶어줌

### 바운디드 컨텍스트로 묶기

![image](https://user-images.githubusercontent.com/45786659/118963396-5f057b00-b9a1-11eb-9289-909a85d38d82.png)

    - 도메인 서열 분리 
        - Core Domain:  룸, 예약 : 없어서는 안될 핵심 서비스이며, 연견 Up-time SLA 수준을 99.999% 목표, 배포주기는 1주일 1회 미만
        - Supporting Domain:   알림, 마이페이지 : 경쟁력을 내기위한 서비스이며, SLA 수준은 연간 60% 이상 uptime 목표, 배포주기는 각 팀의 자율이나 표준 스프린트 주기가 1주일 이므로 1주일 1회 이상을 기준으로 함.
        - General Domain:   결제 : 결제서비스로 3rd Party 외부 서비스를 사용하는 것이 경쟁력이 높음 (핑크색으로 이후 전환할 예정)

### 폴리시 부착 (괄호는 수행주체, 폴리시 부착을 둘째단계에서 해놔도 상관 없음. 전체 연계가 초기에 드러남)

![image](https://user-images.githubusercontent.com/45786659/118963431-64fb5c00-b9a1-11eb-9064-e3a5dbebdd34.png)

### 폴리시의 이동과 컨텍스트 매핑 (점선은 Pub/Sub, 실선은 Req/Resp)

![image](https://user-images.githubusercontent.com/45786659/118963450-6a58a680-b9a1-11eb-9ee3-0a37983b6081.png)
    
### 호텔 바운디드 컨텍스트 내 PUB/SUB 고려하여 모델 간소화

![image](https://user-images.githubusercontent.com/43338817/118920251-03bb9480-b971-11eb-8b0d-439315192a0d.png)

### 기능적/비기능적 요구사항을 커버하는지 검증

![image](https://user-images.githubusercontent.com/45786659/118925407-abd55b80-b979-11eb-8fd8-aa1a350a5a85.png)

    - (ok) 호스트가 룸을 등록한다.
    - (ok) 호스트가 룸을 삭제한다.

![image](https://user-images.githubusercontent.com/45786659/118925428-b2fc6980-b979-11eb-9c07-97f79c70d1b4.png)

    - (ok) 게스트가 룸을 검색한다.
    - (ok) 게스트가 룸을 선택하여 사용 예약한다.
    - (ok) 게스트가 결제한다. (Sync, 결제서비스)
    - (ok) 결제가 완료되면, 결제 & 예약 내용을 게스트에게 알림을 전송한다. (Async, 알림서비스)
    - (ok) 예약 내역을 호스트에게 전달한다.
    
![image](https://user-images.githubusercontent.com/45786659/118925449-bbed3b00-b979-11eb-9127-cd5ebe06825a.png)

    - (ok) 게스트는 본인의 예약 내용 및 상태를 조회한다.
    - (ok) 게스트는 본인의 예약을 취소할 수 있다.
    - (ok) 예약이 취소되면, 결제를 취소한다. (Async, 결제서비스)
    - (ok) 결제가 취소되면, 결제 취소 내용을 게스트에게 알림을 전송한다. (Async, 알림서비스)



### 비기능 요구사항에 대한 검증

![image](https://user-images.githubusercontent.com/45786659/118966467-bd802880-b9a4-11eb-8e92-abbc04826774.png)

    - 마이크로 서비스를 넘나드는 시나리오에 대한 트랜잭션 처리
        - 룸 예약시 결제처리: 결제가 완료되지 않은 예약은 절대 받지 않는다에 따라, ACID 트랜잭션 적용. 예약 완료시 결제처리에 대해서는 Request-Response 방식 처리
        - 예약 완료시 알림 처리: 예약에서 알림 마이크로서비스로 예약 완료 내용이 전달되는 과정에 있어서 알림 마이크로서비스가 별도의 배포주기를 가지기 때문에 Eventual Consistency 방식으로 트랜잭션 처리함.
        - 나머지 모든 inter-microservice 트랜잭션: 예약상태, 예약취소 등 모든 이벤트에 대해 알림 처리하는 등, 데이터 일관성의 시점이 크리티컬하지 않은 모든 경우가 대부분이라 판단, Eventual Consistency 를 기본으로 채택함.


## 헥사고날 아키텍처 다이어그램 도출
    
![image](https://user-images.githubusercontent.com/45786659/118924241-c4dd0d00-b977-11eb-94b2-c49d3b4e3de5.png)


    - Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
    - 호출관계에서 PubSub 과 Req/Resp 를 구분함
    - 서브 도메인과 바운디드 컨텍스트의 분리:  각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐


# 구현:

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 배포는 아래와 같이 수행한다.

```
# eks cluster 생성
eksctl create cluster --name example-hotel-booking --version 1.16 --nodegroup-name standard-workers --node-type t3.medium --nodes 3 --nodes-min 1 --nodes-max 4

# eks cluster 설정
aws eks --region ap-northeast-1 update-kubeconfig --name example-hotel-booking
kubectl config current-context

# metric server 설치
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/download/v0.3.6/components.yaml

# Helm 설치
curl https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3 > get_helm.sh
chmod 700 get_helm.sh
./get_helm.sh
(Helm 에게 권한을 부여하고 초기화)
kubectl --namespace kube-system create sa tiller
kubectl create clusterrolebinding tiller --clusterrole cluster-admin --serviceaccount=kube-system:tiller
helm init --service-account tiller

# Kafka 설치
helm repo update
helm repo add bitnami https://charts.bitnami.com/bitnami
kubectl create ns kafka
helm install my-kafka bitnami/kafka --namespace kafka

# myhotel namespace 생성
kubectl create namespace myhotel

# myhotel image build & push
cd myhotel/book
mvn package
docker build -t 740569282574.dkr.ecr.ap-northeast-1.amazonaws.com/book:latest .
docker push 740569282574.dkr.ecr.ap-northeast-1.amazonaws.com/book:latest

# myhotel deploy
cd myhotel/yaml
kubectl apply -f configmap.yaml
kubectl apply -f gateway.yaml
kubectl apply -f room.yaml
kubectl apply -f book.yaml
kubectl apply -f pay.yaml
kubectl apply -f mypage.yaml
kubectl apply -f alarm.yaml
kubectl apply -f siege.yaml
```

현황

![image](https://user-images.githubusercontent.com/45786659/118969619-3b91fe80-b9a8-11eb-9594-eb9fb4efde49.png)

![image](https://user-images.githubusercontent.com/45786659/118969712-595f6380-b9a8-11eb-947a-ca627e5e15e6.png)

![image](https://user-images.githubusercontent.com/45786659/118971988-04711c80-b9ab-11eb-800b-be935df5235d.png)



## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다: (예시는 book 마이크로 서비스). 이때 가능한 현업에서 사용하는 언어 (유비쿼터스 랭귀지)를 그대로 사용하려고 노력했다. 

```
package intensiveteam;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;
import intensiveteam.external.*;

@Entity
@Table(name="Book_table")
public class Book {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    private Long bookId;
    private Long roomId;
    private Integer price;
    private Long hostId;
    private Long guestId;
    private Date startDate;
    private Date endDate;
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }

    public Long getGuestId() {
        return guestId;
    }

    public void setGuestId(Long guestId) {
        this.guestId = guestId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}

```
- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다
```
package intensiveteam;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="books", path="books")
public interface BookRepository extends PagingAndSortingRepository<Book, Long>{


}
```
- 적용 후 REST API 의 테스트
```
# 룸 등록처리
http POST http://room:8080/rooms price=1500

# 예약처리
http POST http://book:8080/books roomId=1 price=1000 startDate=20210505 endDate=20210508

# 예약 상태 확인
http http://book:8080/books/1

```


## 폴리글랏 퍼시스턴스



## 폴리글랏 프로그래밍



## 동기식 호출 과 Fallback 처리

분석단계에서의 조건 중 하나로 예약(book)->결제(pay) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다. 

- 결제 서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현 

```
# (book) PaymentService.java 

@FeignClient(name="pay", url="http://pay:8080")
public interface PaymentService {

    @RequestMapping(method= RequestMethod.GET, path="/payments")
    public void pay(@RequestBody Payment payment);

}
```

- 예약을 받은 직후(@PostPersist) 결제를 요청하도록 처리
```
@Entity
@Table(name="Book_table")
public class Book {
    
    ...

    @PostPersist
    public void onPostPersist(){
        {

            intensiveteam.external.Payment payment = new intensiveteam.external.Payment();
            payment.setBookId(getId());
            payment.setRoomId(getRoomId());
            payment.setGuestId(getGuestId());
            payment.setPrice(getPrice());
            payment.setHostId(getHostId());
            payment.setStartDate(getStartDate());
            payment.setEndDate(getEndDate());
            payment.setStatus("PayApproved");

            // mappings goes here
            try {
                 BookApplication.applicationContext.getBean(intensiveteam.external.PaymentService.class)
                    .pay(payment);
            }catch(Exception e) {
                throw new RuntimeException("결제서비스 호출 실패입니다.");
            }
        }

}
```

- 동기식 호출로 연결되어 있는 예약(book)->결제(pay) 간의 연결 상황을 Kiali Graph로 확인한 결과 (siege 이용하여 book POST)

![image](https://user-images.githubusercontent.com/43338817/119081473-fec11880-ba36-11eb-83fe-ef94952faef1.png)

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 결제 시스템이 장애가 나면 주문도 못받는다는 것을 확인:


```
# 결제 서비스를 잠시 내려놓음
cd yaml
$ kubectl delete -f pay.yaml
```
![image](https://user-images.githubusercontent.com/45786659/119074505-252c8700-ba2a-11eb-89cd-8151b2b757e4.png)
```
# 예약처리 (siege 사용)
http POST http://book:8080/books roomId=2 price=1500 startDate=20210505 endDate=20210508  #Fail
http POST http://book:8080/books roomId=3 price=2000 startDate=20210505 endDate=20210508  #Fail
```
![image](https://user-images.githubusercontent.com/45786659/119074532-2f4e8580-ba2a-11eb-81dd-1b0b4c058b18.png)

```
# 결제서비스 재기동
$ kubectl apply -f pay.yaml
```
![image](https://user-images.githubusercontent.com/45786659/119074868-c4ea1500-ba2a-11eb-8ae4-7b4c04945b43.png)
```
# 예약처리 (siege 사용)
http POST http://book:8080/books roomId=2 price=1500 startDate=20210505 endDate=20210508  #Success
http POST http://book:8080/books roomId=3 price=2000 startDate=20210505 endDate=20210508  #Success
```
![image](https://user-images.githubusercontent.com/45786659/119074931-e4813d80-ba2a-11eb-9a42-623e8513ddb1.png)



- 또한 과도한 요청시에 서비스 장애가 도미노 처럼 벌어질 수 있다. (서킷브레이커, 폴백 처리는 운영단계에서 설명한다.)




## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트


결제가 이루어진 후에 알림 처리는 동기식이 아니라 비 동기식으로 처리하여 알림 시스템의 처리를 위하여 예약 블로킹 되지 않아도록 처리한다.
 
- 이를 위하여 예약관리, 결제이력에 기록을 남긴 후에 곧바로 결제승인이 되었다는 도메인 이벤트를 카프카로 송출한다(Publish)
 
```
@Entity
@Table(name="Payment_table")
public class Payment {

    ...

    @PostPersist
    public void onPostPersist(){
        PaymentApproved paymentApproved = new PaymentApproved();
        BeanUtils.copyProperties(this, paymentApproved);
        paymentApproved.publishAfterCommit();


    }

}
```
- 알림 서비스에서는 예약완료, 결제승인 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler를 구현한다:

```
@Service
public class PolicyHandler{

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentApproved_Notify(@Payload PaymentApproved paymentApproved){

        if(!paymentApproved.validate()) return;

        System.out.println("\n\n##### listener Notify : " + paymentApproved.toJson() + "\n\n");

        // Sample Logic //
        Notification notification = new Notification();
        notificationRepository.save(notification);
            
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentCanceled_Notify(@Payload PaymentCanceled paymentCanceled){

        if(!paymentCanceled.validate()) return;

        System.out.println("\n\n##### listener Notify : " + paymentCanceled.toJson() + "\n\n");

        // Sample Logic //
        Notification notification = new Notification();
        notificationRepository.save(notification);
            
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverBookCanceled_Notify(@Payload BookCanceled bookCanceled){

        if(!bookCanceled.validate()) return;

        System.out.println("\n\n##### listener Notify : " + bookCanceled.toJson() + "\n\n");

        // Sample Logic //
        Notification notification = new Notification();
        notificationRepository.save(notification);
            
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverBooked_Notify(@Payload Booked booked){

        if(!booked.validate()) return;

        System.out.println("\n\n##### listener Notify : " + booked.toJson() + "\n\n");

        // Sample Logic //
        Notification notification = new Notification();
        notificationRepository.save(notification);
            
    }

```

알림 시스템은 예약/결제와 완전히 분리되어있으며, 이벤트 수신에 따라 처리되기 때문에, 알림 시스템이 유지보수로 인해 잠시 내려간 상태라도 예약을 받는데 문제가 없다:
```
# 알림 서비스를 잠시 내려놓음
cd yaml
kubectl delete -f alarm.yaml
```

![image](https://user-images.githubusercontent.com/45786659/119075963-aedd5400-ba2c-11eb-950b-342bdb58be3d.png)

```
# 예약처리 (siege 사용)
http POST http://book:8080/books roomId=2 price=1500 startDate=20210505 endDate=20210508	#Success
http POST http://book:8080/books roomId=3 price=2000 startDate=20210505 endDate=20210508	#Success
```

![image](https://user-images.githubusercontent.com/45786659/119076006-c74d6e80-ba2c-11eb-9d70-3a08a5bb3ec0.png)

```
# 알림이력 확인 (siege 사용)
http http://alarm:8080/notifications # 알림이력조회 불가
```

![image](https://user-images.githubusercontent.com/45786659/119076052-daf8d500-ba2c-11eb-81d3-e8a1ddebe287.png)

```
# 알림 서비스 기동
kubectl apply -f alarm.yaml
```

![image](https://user-images.githubusercontent.com/45786659/119076341-5b1f3a80-ba2d-11eb-8a1f-5554c46233cf.png)

```
# 알림이력 확인 (siege 사용)
http http://alarm:8080/notifications # 알림이력조회
```
![image](https://user-images.githubusercontent.com/45786659/119076408-7722dc00-ba2d-11eb-9a01-766f4dd6f9ca.png)

## Correlation 테스트

서비스를 이용해 만들어진 각 이벤트 건은 Correlation-key 연결을 통해 식별이 가능하다.

- Correlation-key로 식별하여 예약(book) 이벤트를 통해 생성된 결제(pay) 건에 대해 예약 취소 시 동일한 Correlation-key를 가지는 결제(pay) 이벤트 건 역시 삭제되는 모습을 확인한다:

결제(pay) 이벤트 건 확인을 위하여 GET 수행

<img width="1440" alt="스크린샷 2021-05-21 오후 4 56 23" src="https://user-images.githubusercontent.com/43338817/119104024-c2051980-ba56-11eb-9dc5-b49c410ad5f3.png">

위 결제(pay) 이벤트 건과 동일한 식별 키를 갖는 7번 예약(book) 이벤트 건 DELETE 수행

<img width="1440" alt="스크린샷 2021-05-21 오후 4 56 58" src="https://user-images.githubusercontent.com/43338817/119103981-b6195780-ba56-11eb-8eae-81bb47dc1b09.png">

결제(pay) 이벤트 건을 GET 명령어를 통해 조회한 결과 예약(book)에서 삭제한 7번 키를 갖는 결제(pay) 이벤트 또한 삭제된 것을 확

<img width="1440" alt="스크린샷 2021-05-21 오후 4 57 10" src="https://user-images.githubusercontent.com/43338817/119104058-c92c2780-ba56-11eb-83bc-9cc1baff8157.png">

# 운영

## CI/CD 설정


각 구현체들은 각자의 source repository 에 구성되었고, 사용한 CI/CD 플랫폼은 GCP를 사용하였으며, pipeline build script 는 각 프로젝트 폴더 이하에 cloudbuild.yml 에 포함되었다.


## 동기식 호출 / 서킷 브레이킹 / 장애격리

### 서킷 브레이킹 프레임워크의 선택 : istio-injection + DestinationRule

- istio-injection 적용 (기 적용 완료)
```
kubectl label namespace myhotel istio-injection=enabled --overwrite
```
- 예약, 결제 서비스 모두 아무런 변경 없음
- 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인:
- 동시사용자 255명
- 180초 동안 실시
```
$ siege -v -c255 -t180S -r10 --content-type "application/json" 'http://book:8080/books POST {"bookId":1, "roomId":1, "price":1000, "hostId":10, "guestId":10, "startDate":20200101, "endDate":20200103}'
```
- 서킷브레이킹을 위한 DestinationRule 적용
```
cd myhotel/yaml
kubectl apply -f dr-pay.yaml

# dr-pay.yaml
apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: dr-pay
  namespace: myhotel
spec:
  host: pay
  trafficPolicy:
    connectionPool:
      http:
        http1MaxPendingRequests: 1
        maxRequestsPerConnection: 1
    outlierDetection:
      interval: 1s
      consecutiveErrors: 2
      baseEjectionTime: 10s
      maxEjectionPercent: 100
```

- DestinationRule 적용되어 서킷 브레이킹 동작 확인 (Kiali Graph)

![image](https://user-images.githubusercontent.com/43338817/119082429-0bdf0700-ba39-11eb-8f29-b0f934c9c4b5.png)

- 다시 부하 발생하여 DestinationRule 적용 제거하여 정상 처리 확인
```
cd myhotel/yaml
kubectl delete -f dr-pay.yaml
```

## 오토스케일 아웃
앞서 CB 는 시스템을 안정되게 운영할 수 있게 해줬지만 사용자의 요청을 100% 받아들여주지 못했기 때문에 이에 대한 보완책으로 자동화된 확장 기능을 적용하고자 한다. 


- 결제서비스에 대한 replica 를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 CPU 사용량이 3프로를 넘어서면 replica 를 10개까지 늘려준다:
```
$ kubectl autoscale deploy pay --min=1 --max=10 --cpu-percent=3
```
- 오토스케일 아웃 테스트를 위하여 book.yaml 파일 spec indent에 메모리 설정에 대한 문구를 추가한다:

![image](https://user-images.githubusercontent.com/45786659/119083688-64af9f00-ba3b-11eb-9c58-7966c141afee.png)

- CB 에서 했던 방식대로 워크로드를 3분 동안 걸어준다.
```
siege -v -c255 -t180S -r10 --content-type "application/json" 'http://book:8080/books POST {"bookId":1, "roomId":1, "price":1000, "hostId":10, "guestId":10, "startDate":20200101, "endDate":20200103}'
```
- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다:
```
kubectl get deploy book -w -n myhotel
```
- 어느정도 시간이 흐른 후 (약 30초) 스케일 아웃이 벌어지는 것을 확인할 수 있다:
```
NAME   READY   UP-TO-DATE   AVAILABLE   AGE
book   1/1     1            1           66s
book   1/4     1            1           2m9s
book   1/4     1            1           2m9s
book   1/4     1            1           2m9s
book   1/4     4            1           2m9s
book   1/8     4            1           2m24s
book   1/8     4            1           2m24s
book   1/8     4            1           2m24s
book   1/8     8            1           2m24s
book   1/10    8            1           2m40s
book   1/10    8            1           2m40s
book   1/10    8            1           2m40s
book   1/10    10           1           2m40s
book   2/10    10           2           3m21s
book   3/10    10           3           3m26s
book   4/10    10           4           3m35s
book   5/10    10           5           3m39s
book   6/10    10           6           3m41s
book   7/10    10           7           3m42s
book   8/10    10           8           3m43s
book   9/10    10           9           3m54s
book   10/10   10           10          3m55s
:
```
- siege 의 로그를 보아도 전체적인 성공률이 높아진 것을 확인 할 수 있다. 
```
Transactions:                   9090 hits
Availability:                  99.98 %
Elapsed time:                 143.38 secs
Data transferred:               3.06 MB
Response time:                  3.88 secs
Transaction rate:              63.40 trans/sec
Throughput:                     0.02 MB/sec
Concurrency:                  245.75
Successful transactions:        9090
Failed transactions:               2
Longest transaction:           34.12
Shortest transaction:           0.01
```


## 무정지 재배포

- 먼저 무정지 재배포가 100% 되는 것인지 확인하기 위해서 Autoscaler 이나 CB 설정을 제거함
- seige 로 배포작업 직전에 워크로드를 모니터링 함.



==> Readiness 없을 경우 배포 시

```
$ siege -c1 -t60S -v http://room:8080/rooms --delay=1S

```

```
kubectl apply -f room.yaml

```

![image](https://user-images.githubusercontent.com/81946702/119099119-887ddf80-ba51-11eb-879c-e30d4819f17a.png)



- seige 의 화면으로 넘어가서 Availability 가 100% 미만으로 떨어졌는지 확인

![image](https://user-images.githubusercontent.com/81946702/119099287-b8c57e00-ba51-11eb-8a5a-991f7d3e6037.png)


==> Readiness 추가 후 배포

- 새버전으로의 배포 시작

- room.yml 에 readiessProbe 설정

```
(room.yml)
          readinessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 10
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 10
```
- room.yml 적용

```
kubectl apply -f room.yaml

```
- 동일한 시나리오로 재배포 한 후 Availability 확인:

![image](https://user-images.githubusercontent.com/81946702/119099653-16f26100-ba52-11eb-82da-f04219eabd38.png)

Availability 100%인 것 확인



## ConfigMap 사용

시스템별로 또는 운영중에 동적으로 변경 가능성이 있는 설정들을 ConfigMap을 사용하여 관리합니다.

* configmap.yaml
```
apiVersion: v1
kind: ConfigMap
metadata:
  name: myhotel-config
  namespace: myhotel
data:
  api.url.payment: http://pay:8080
  alarm.prefix: Hello
```
* book.yaml (configmap 사용)
```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: book
  namespace: myhotel
  labels:
    app: book
spec:
  replicas: 1
  selector:
    matchLabels:
      app: book
  template:
    metadata:
      labels:
        app: book
    spec:
      containers:
        - name: book
          image: 740569282574.dkr.ecr.ap-northeast-1.amazonaws.com/book:latest
          imagePullPolicy: Always
          ports:
            - containerPort: 8080
          env:
            - name: api.url.payment
              valueFrom:
                configMapKeyRef:
                  name: myhotel-config
                  key: api.url.payment
          ...
```
* kubectl describe pod/book-77998c895-ffbnn -n myhotel
```
Containers:
  book:
    Container ID:   docker://22dff5a6bd54a48951dc328db052ca494295dae7a431384b920714a5d6814b43
    Image:          740569282574.dkr.ecr.ap-northeast-1.amazonaws.com/book:latest
    Image ID:       docker-pullable://740569282574.dkr.ecr.ap-northeast-1.amazonaws.com/book@sha256:4918ad3d2dc44648151861f0d94457a02c963823df863a702f9bb05c7ac02261
    Port:           8080/TCP
    Host Port:      0/TCP
    State:          Running
      Started:      Fri, 21 May 2021 02:21:12 +0000
    Ready:          True
    Restart Count:  0
    Liveness:       http-get http://:8080/actuator/health delay=120s timeout=2s period=5s #success=1 #failure=5
    Readiness:      http-get http://:8080/actuator/health delay=10s timeout=2s period=5s #success=1 #failure=10
    Environment:
      api.url.payment:  <set to the key 'api.url.payment' of config map 'myhotel-config'>  Optional: false
    Mounts:
      /var/run/secrets/kubernetes.io/serviceaccount from default-token-m9sfp (ro)
```

## Self-healing (Liveness Probe)

deployment.yml 에 Liveness Probe 옵션 설정

```
(book/kubernetes/deployment.yml)
          ...
          
          livenessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 120
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 5
```

book pod에 liveness가 적용된 부분 확인

```
kubectl describe pod/book-77998c895-ffbnn -n myhotel
```
![image](https://user-images.githubusercontent.com/45786659/119081667-6b3c1780-ba37-11eb-881c-197b181a4e43.png)


book 서비스의 liveness가 발동되어 2회 retry 시도 한 부분 확인
```
kubectl get -n myhotel all
```
![image](https://user-images.githubusercontent.com/45786659/119081060-311e4600-ba36-11eb-8112-7fd52411f941.png)

retry 시도 상황 재현을 위하여 부하테스터 siege를 활용하여 book 과부하
```
$ siege -v -c255 -t180S -r10 --content-type "application/json" 'http://book:8080/books POST {"bookId":1, "roomId":1, "price":1000, "hostId":10, "guestId":10, "startDate":20200101, "endDate":20200103}'

# Service Unavailable(503) 오류 
HTTP/1.1 500    31.60 secs:     820 bytes ==> POST http://book:8080/books
HTTP/1.1 500    31.48 secs:     820 bytes ==> POST http://book:8080/books
HTTP/1.1 500    31.40 secs:     820 bytes ==> POST http://book:8080/books
HTTP/1.1 500    31.70 secs:     820 bytes ==> POST http://book:8080/books
HTTP/1.1 500    31.61 secs:     820 bytes ==> POST http://book:8080/books
HTTP/1.1 500    31.71 secs:     820 bytes ==> POST http://book:8080/books
HTTP/1.1 500    31.59 secs:     820 bytes ==> POST http://book:8080/books
HTTP/1.1 500    31.52 secs:     820 bytes ==> POST http://book:8080/books
HTTP/1.1 500    31.60 secs:     820 bytes ==> POST http://book:8080/books
HTTP/1.1 500    31.60 secs:     820 bytes ==> POST http://book:8080/books
HTTP/1.1 503    31.75 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    28.85 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    31.06 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    30.26 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    29.56 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    29.25 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    29.17 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    31.17 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    30.57 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    30.26 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    29.25 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    30.07 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    31.17 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    28.17 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    31.67 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    28.97 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    31.37 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    29.55 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    29.26 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    29.16 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    29.66 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    30.73 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    28.86 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    31.46 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    29.86 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    29.25 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    30.06 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    28.57 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    29.97 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    31.36 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    29.75 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    29.25 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    30.05 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    30.67 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    30.96 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    29.67 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    29.38 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    29.18 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    29.48 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    30.27 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    30.58 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    30.87 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    30.68 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    30.68 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    29.27 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    31.38 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    30.17 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    31.17 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    29.26 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    30.17 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    30.68 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    29.58 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    29.68 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    31.18 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    28.66 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    30.38 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    31.77 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    32.08 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    29.58 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    29.87 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    31.18 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    30.38 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    31.38 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    30.87 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    30.48 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    30.87 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    31.08 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    29.38 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    28.66 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    30.98 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    28.77 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    30.87 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    29.08 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    31.46 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    31.08 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    29.87 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    28.77 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    28.77 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    30.48 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    30.07 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    30.38 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    31.39 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    28.58 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    30.89 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    30.29 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    29.49 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    31.69 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    30.09 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    31.29 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    28.86 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    30.88 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    29.49 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    30.58 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    28.67 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    29.09 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    29.08 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    31.19 secs:      19 bytes ==> POST http://book:8080/books
HTTP/1.1 503    29.09 secs:      19 bytes ==> POST http://book:8080/books

# book deploy 모니터링 결과 (Service Unavailable 확인)
NAME   READY   UP-TO-DATE   AVAILABLE   AGE
book   1/1     1            1           116s
book   0/1     1            0           7m48s

# book deploy 모니터링 결과 (Liveness 발동되어 Retry)
$ kubectl get deploy book -w -n myhotel

NAME   READY   UP-TO-DATE   AVAILABLE   AGE
book   1/1     1            1           116s
book   0/1     1            0           7m48s
book   1/1     1            1           8m57s

# book pod 조회 결과 (Liveness 발동되어 Retry)
$ kubectl get pod/book-6f6db947f7-kqggb -n myhotel -o wide
NAME                    READY   STATUS    RESTARTS   AGE   IP             NODE                                               NOMINATED NODE   READINESS GATES
book-6f6db947f7-kqggb   2/2     Running   1          18m   192.168.0.47   ip-192-168-2-193.ap-northeast-1.compute.internal   <none>           <none>
```

## CQRS

- 게스트와 호스트가 자주 예약관리에서 확인할 수 있는 상태를 마이페이지(프론트엔드)에서 확인할 수 있어야 한다:

<img width="1440" alt="cqrs" src="https://user-images.githubusercontent.com/45786659/119085956-a9d5d000-ba3f-11eb-80a6-7c3210c02823.png">


# 신규 조직의 추가

## 마켓팅팀 추가

![image](https://user-images.githubusercontent.com/11704927/120576924-8018a200-c45e-11eb-9a48-009cabbe950a.png)



## 고객센터 추가
