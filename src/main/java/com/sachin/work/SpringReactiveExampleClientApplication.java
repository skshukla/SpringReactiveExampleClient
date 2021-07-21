package com.sachin.work;

import io.rsocket.RSocket;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class SpringReactiveExampleClientApplication {

	@Bean
	RSocketRequester rSocketRequester(RSocketRequester.Builder builder) {
		return builder.connectTcp("localhost", 7000).block();
	}

	public static void main(String[] args) throws Exception{
		SpringApplication.run(SpringReactiveExampleClientApplication.class, args);
		Thread.sleep(60 * 1000);
	}

}

@Component
class Client {

	@Autowired
	private RSocketRequester rSocketRequester;


	@EventListener(ApplicationReadyEvent.class)
	public void execute() {
		System.out.println("Consumer is ready..");
		int N = 10000;
		long begin = System.currentTimeMillis();
		for(int i=0;i<N;i++) {
			final int j = i;
			rSocketRequester.route("hello")
					.data(new MyRequestVO("sachin abcd"))
					.retrieveMono(MyResponseVO.class)
					.subscribe(e -> {
						if (j%500 == 0) {
							System.out.println(String.format("i {%d}, Body {%s}", j, e));
						}
						if (j == N-1) {
							long timeTaken = System.currentTimeMillis() - begin;
							System.out.println(String.format("RSocket BenchMarking : Time taken is {%d} ms to complete {%d} calls", timeTaken, N));
						}
					});

		}


	}





//	  @EventListener(ApplicationReadyEvent.class)
	public void executeNormal() {
		int N=10000;
		System.out.println("Normal Consumer is ready..");
		RestTemplate rt = new RestTemplate();

		long begin = System.currentTimeMillis();
		for(int i=0;i<N;i++) {
			ResponseEntity<String> responseEntity = rt.getForEntity("http://localhost:8080/random", String.class);
			if (i%500 == 0) {
				System.out.println(String.format("i {%d}, Body {%s}", i, responseEntity.getBody()));
			}
		}
		long timeTaken = System.currentTimeMillis() - begin;

		System.out.println(String.format("HTTP BenchMarking : Time taken is {%d} ms to complete {%d} calls", timeTaken, N));
	}
}



@Data
@NoArgsConstructor
@AllArgsConstructor
class MyRequestVO {
	private String name;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class MyResponseVO {
	private String msg;
}
