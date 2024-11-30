# 자동 구성(Auto Configuration)

**정리**

회원 데이터를 DB에 보관하고 관리하기 위해 앞서 빈으로 등록한 `JdbcTemplate` , `DataSource` ,`TransactionManager` 가 모두 사용되었다.

그런데 생각해보면 DB에 데이터를 보관하고 관리하기 위해 이런 객체 들을 항상 스프링 빈으로 등록해야 하는 번거로움이 있다. 

만약 DB를 사용하는 다른 프로젝트를 진행한다면 이러한 객체들을 또 스프링 빈으로 등록해야 할 것이다.

이거 또한 스프링부트가 해결해준다.

## 자동 구성 확인

DbConfig의 @Configuration를 주석해보고 실행해도 모두 빈으로 등록된다.

사실 이 빈들은 모두 스프링 부트가 자동을 등록해준다.

## 스프링 부트의 자동 구성

스프링 부트는 자동 구성(Auto Configuration)이라는 기능을 제공하는데, 일반적으로 자주 사용하는 수 많은 빈들을 자동으로 등록해주는 기능이다.

앞서 우리가 살펴보았던 `JdbcTemplate` , `DataSource` , `TransactionManager` 모두 스프링 부트가 자동 구성을 제공해서 자동으로 스프링 빈으로 등록된다.

이러한 자동 구성 덕분에 개발자는 반복적이고 복잡한 빈 등록과 설정을 최소화하고 애플리케이션 개발을 빠르게 시작 할 수 있다.

### 자동 구성 살짝 알아보기

스프링 부트는 `spring-boot-autoconfigure` 라는 프로젝트 안에서 수 많은 자동 구성을 제공한다. 

`JdbcTemplate` 을 설정하고 빈으로 등록해주는 자동 구성을 확인해보자.

```java
@AutoConfiguration(after = DataSourceAutoConfiguration.class)
@ConditionalOnClass({ DataSource.class, JdbcTemplate.class })
@ConditionalOnSingleCandidate(DataSource.class)
@EnableConfigurationProperties(JdbcProperties.class)
@Import({ DatabaseInitializationDependencyConfigurer.class, JdbcTemplateConfiguration.class,
		NamedParameterJdbcTemplateConfiguration.class })
public class JdbcTemplateAutoConfiguration {

}
```

* `@AutoConfiguration` : 자동 구성을 사용하려면 이 애노테이션을 등록해야 한다.
  * 자동 구성도 내부에 `@Configuration` 이 있어서 빈을 등록하는 자바 설정 파일로 사용할 수 있다. 
  * `after = DataSourceAutoConfiguration.class`
    * 자동 구성이 실행되는 순서를 지정할 수 있다. `JdbcTemplate` 은 `DataSource` 가 필요하기 때문에 `DataSource` 를 자동으로 등록해주는 `DataSourceAutoConfiguration` 다음에 실행 하도록 설정되어 있다.
* `@ConditionalOnClass({ DataSource.class, JdbcTemplate.class })`
  * IF문과 유사한 기능을 제공한다. 이런 클래스가 있는 경우에만 설정이 동작한다. 만약 없으면 여기 있는 설정들이 모두 무효화 되고, 빈도 등록되지 않는다.
  * `@ConditionalXxx` 시리즈가 있다. 자동 구성의 핵심이므로 뒤에서 자세히 알아본다.
  * `JdbcTemplate` 은 `DataSource` , `JdbcTemplate` 라는 클래스가 있어야 동작할 수 있다. 
* `@Import` : 스프링에서 자바 설정을 추가할 때 사용한다.

`@Import` 의 대상이 되는 `JdbcTemplateConfiguration` 를 추가로 확인해보자.

```java
@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean(JdbcOperations.class)
class JdbcTemplateConfiguration {

	@Bean
	@Primary
	JdbcTemplate jdbcTemplate(DataSource dataSource, JdbcProperties properties) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		JdbcProperties.Template template = properties.getTemplate();
		jdbcTemplate.setFetchSize(template.getFetchSize());
		jdbcTemplate.setMaxRows(template.getMaxRows());
		if (template.getQueryTimeout() != null) {
			jdbcTemplate.setQueryTimeout((int) template.getQueryTimeout().getSeconds());
		}
		return jdbcTemplate;
	}
}
```

* `@Configuration` : 자바 설정 파일로 사용된다. 
* `@ConditionalOnMissingBean(JdbcOperations.class)` `JdbcOperations` 빈이 없을 때 동작한다.
  * `JdbcTemplate` 의 부모 인터페이스가 바로 `JdbcOperations` 이다.
  * 쉽게 이야기해서 `JdbcTemplate` 이 빈으로 등록되어 있지 않은 경우에만 동작한다.
  * 만약 이런 기능이 없으면 내가 등록한 `JdbcTemplate` 과 자동 구성이 등록하는 `JdbcTemplate`
  * 이 중복 등록되는 문제가 발생할 수 있다.
  * 보통 개발자가 직접 빈을 등록하면 개발자가 등록한 빈을 사용하고, 자동 구성은 동작하지 않는다.
* `JdbcTemplate` 이 몇가지 설정을 거쳐서 빈으로 등록되는 것을 확인할 수 있다.

**자동 등록 설정**

다음과 같은 자동 구성 기능들이 다음 빈들을 등록해준다.

* `JdbcTemplateAutoConfiguration` : `JdbcTemplate` 
* `DataSourceAutoConfiguration` : `DataSource` 
* `DataSourceTransactionManagerAutoConfiguration` : `TransactionManager`

그래서 개발자가 직접 빈을 등록하지 않아도 `JdbcTemplate` , `DataSource` , `TransactionManager` 가 스프링 빈으로 등록된 것이다.

**스프링 부트가 제공하는 자동 구성(AutoConfiguration)**

https://docs.spring.io/spring-boot/docs/current/reference/html/auto-configuration-classes.html

스프링 부트는 수 많은 자동 구성을 제공하고 `spring-boot-autoconfigure` 에 자동 구성을 모아둔다.

스프링 부트 프로젝트를 사용하면 `spring-boot-autoconfigure` 라이브러리는 기본적으로 사용된다.

### Auto Configuration - 용어, 자동 설정? 자동 구성? 

Auto Configuration은 주로 다음 두 용어로 번역되어 사용된다.

* 자동 설정 
* 자동 구성

**자동 설정**

`Configuration` 이라는 단어가 컴퓨터 용어에서는 환경 설정, 설정이라는 뜻으로 자주 사용된다. 

AutoConfiguration은 크게 보면 빈들을 자동으로 등록해서 스프링이 동작하는 환경을 자동으로 설정해주기 때문에 자동 설정이라는 용어도 맞다.

**자동 구성**

`Configuration` 이라는 단어는 구성, 배치라는 뜻도 있다.

예를 들어서 컴퓨터라고 하면 CPU, 메모리등을 배치해야 컴퓨터가 동작한다. 이렇게 배치하는 것을 구성이라 한다. 

스프링도 스프링 실행에 필요한 빈들을 적절하게 배치해야 한다. 자동 구성은 스프링 실행에 필요한 빈들을 자동으로 배 치해주는 것이다.

자동 설정, 자동 구성 두 용어 모두 맞는 말이다. 

자동 설정은 넓게 사용되는 의미이고, 자동 구성은 실행에 필요한 컴포 넌트조각을 자동으로 배치한다는 더 좁은 의미에 가깝다.

AutoConfiguration은 

**자동 구성**이라는 단어를 주로 사용하고, 문맥에 따라서 자동 설정이라는 단어도 사용하겠다.

Configuration이 단독으로 사용될 때는 **설정**이라는 단어를 사용하겠다.

**정리**

스프링 부트가 제공하는 자동 구성 기능을 이해하려면 다음 두 가지 개념을 이해해야 한다.

`@Conditional` : 특정 조건에 맞을 때 설정이 동작하도록 한다. 

`@AutoConfiguration` : 자동 구성이 어떻게 동작하는지 내부 원리 이해

## 자동 구성 직접 만들기 - 기반 예제

자동 구성에 대해서 자세히 알아보기 위해 간단한 예제를 만들어보자.

memory 패키지 및 MemoryConfig 확안

JVM에서 메모리 정보를 실시간으로 조회하는 기능이다.

`max` 는 JVM이 사용할 수 있는 최대 메모리, 이 수치를 넘어가면 OOM이 발생한다.

`total` 은 JVM이 확보한 전체 메모리(JVM은 처음부터 `max` 까지 다 확보하지 않고 필요할 때 마다 조금씩 확보한다.)

`free` 는 `total` 중에 사용하지 않은 메모리(JVM이 확보한 전체 메모리 중에 사용하지 않은 것) 

`used` 는 JVM이 사용중인 메모리이다.(`used = total - free` )

## @Conditional

앞서 만든 메모리 조회 기능을 항상 사용하는 것이 아니라 특정 조건일 때만 해당 기능이 활성화 되도록 해 보자.

예를 들어서 개발 서버에서 확인 용도로만 해당 기능을 사용하고, 운영 서버에서는 해당 기능을 사용하지 않는 것이다.

여기서 핵심은 소스코드를 고치지 않고 이런 것이 가능해야 한다는 점이다.

프로젝트를 빌드해서 나온 빌드 파일을 개발 서버에도 배포하고, 같은 파일을 운영서버에도 배포해야 한다.

같은 소스 코드인데 특정 상황일 때만 특정 빈들을 등록해서 사용하도록 도와주는 기능이 바로 `@Conditional` 이다.

참고로 이 기능은 스프링 부트 자동 구성에서 자주 사용한다.

지금부터 `@Conditional` 에 대해서 자세히 알아보자. 이름 그대로 특정 조건을 만족하는가 하지 않는가를 구별하는 기능이다.

이 기능을 사용하려면 먼저 `Condition` 인터페이스를 구현해야 한다. 그전에 잠깐 `Condition` 인터페이스를 살펴보자.

### Condition

```java
package org.springframework.context.annotation;

public interface Condition {
    boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata);
}
```

`matches()` 메서드가 `true` 를 반환하면 조건에 만족해서 동작하고, `false` 를 반환하면 동작하지 않는다.

`ConditionContext` : 스프링 컨테이너, 환경 정보등을 담고 있다.

`AnnotatedTypeMetadata` : 애노테이션 메타 정보를 담고 있다.

`Condition` 인터페이스를 구현해서 다음과 같이 자바 시스템 속성이 `memory=on` 이라고 되어 있을 때만 메모리 기 능이 동작하도록 만들어보자.

```shell
VM Options
java -Dmemory=on -jar project.jar
```

### MemoryCondition

```java
@Slf4j
public class MemoryCondition implements Condition {

  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    String memory = context.getEnvironment().getProperty("memory");

    log.info("memory={}", memory);

    return "on".equals(memory);
  }
}
```

환경 정보에 `memory=on` 이라고 되어 있는 경우에만 `true` 를 반환한다.

**참고: 환경 정보와 관련된 부분은 뒤에서 아주 자세히 다룬다.**

```java
@Configuration
@Conditional(MemoryCondition.class)
public class MemoryConfig {

    @Bean
    public MemoryController memoryController() {
        return new MemoryController(memoryFinder());
    }


    @Bean
    public MemoryFinder memoryFinder() {
        return new MemoryFinder();
    }
}

```

`@Conditional(MemoryCondition.class)`

이제 `MemoryConfig` 의 적용 여부는 `@Conditional` 에 지정한 `MemoryCondition` 의 조건에 따라 달라진다.

`MemoryCondition` 의 `matches()` 를 실행해보고 그 결과가 `true` 이면 `MemoryConfig` 는 정상 동작한다. 

따라서 `memoryController` , `memoryFinder` 가 빈으로 등록된다. 

`MemoryCondition` 의 실행결과가 `false` 이면 `MemoryConfig` 는 무효화 된다. 

그래서 `memoryController` , `memoryFinder` 빈은 등록되지 않는다.

먼저 아무 조건을 주지 않고 실행해보자. 

**실행**

http://localhost:8080/memory
**결과** 

```
Whitelabel Error Page
```

`memory=on` 을 설정하지 않았기 때문에 동작하지 않는다.

다음 로그를 통해서 `MemoryCondition` 조건이 실행된 부분을 확인할 수 있다. 물론 결과는 `false` 를 반환한다.

```
memory.MemoryCondition                   : memory=null
```

이번에는 `memory=on` 조건을 주고 실행해보자.



VM 옵션을 추가하는 경우 `-Dmemory=on` 를 사용해야 한다.

**실행**

http://localhost:8080/memory

**결과** 

```
{"used":24385432,"max":8589934592}
```

`MemoryCondition` 조건이 `true` 를 반환해서 빈이 정상 등록된다. 다음 로그를 확인할 수 있다.

```
memory.MemoryCondition                   : memory=on
memory.MemoryFinder                      : init memoryFinder
```

참고: 스프링이 로딩되는 과정은 복잡해서 `MemoryCondition` 이 여러번 호출될 수 있다. 이 부분은 크게 중요하지 않으니 무시하자.

**참고**

스프링은 외부 설정을 추상화해서 `Environment` 로 통합했다. 

그래서 다음과 같은 다양한 외부 환경 설정 을 `Environment` 하나로 읽어들일 수 있다. 

여기에 대한 더 자세한 내용은 뒤에서 다룬다.

## @Conditional - 다양한 기능

지금까지 `Condition` 인터페이스를 직접 구현해서 `MemoryCondition` 이라는 구현체를 만들었다. 

스프링은 이미 필요한 대부분의 구현체를 만들어두었다. 이번에는 스프링이 제공하는 편리한 기능을 사용해보자.



