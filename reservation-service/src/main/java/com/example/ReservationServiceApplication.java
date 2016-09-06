package com.example;

import java.util.stream.Stream;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;
import lombok.NoArgsConstructor;

@EnableBinding(ReservationChannels.class)
@EnableDiscoveryClient
@SpringBootApplication
public class ReservationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationServiceApplication.class, args);
	}
}

interface ReservationChannels{
	@Input
	SubscribableChannel input();
}

@MessageEndpoint
class ReservationProcessor{
	private final ReservationRepository reservationRepository;
	
	@Autowired
	public ReservationProcessor(ReservationRepository reservationRepository) {
		this.reservationRepository = reservationRepository;
	}
	
	@ServiceActivator(inputChannel="input")
	public void onNewReservation(String reservationName){
		this.reservationRepository.save(new Reservation(reservationName));
	}
}

@RestController
@RefreshScope
class MessageRestController{
	private final String value;
	
	@Autowired
	public MessageRestController(@Value("${message}") String value) {
		this.value = value;
	}
	
	@GetMapping("/message")
	String read(){
		return this.value;
	}
	
}

@Component
class SampleDataCLR implements CommandLineRunner{
	
	private final ReservationRepository reservationRepository;
	
	@Autowired
	public SampleDataCLR(ReservationRepository reservationRepository) {
		this.reservationRepository = reservationRepository;
	}

	@Override
	public void run(String... arg0) throws Exception {
		Stream.of("Vikash", "Hetal", "Mintoo", "Akash", "Shilpa", "Vedu", "Kush")
			.forEach(reservationName -> reservationRepository.save(new Reservation(reservationName)));
		
		reservationRepository.findAll().forEach(System.out::println);
	}
	
}

@RepositoryRestResource
interface ReservationRepository extends JpaRepository<Reservation, Long>{
	
}

@Entity
@NoArgsConstructor
@Data
class Reservation{
	@Id @GeneratedValue
	private Long id;
	
	private String reservationName;
	
	public Reservation(String reservationName){
		this.reservationName=reservationName;
	}
}
