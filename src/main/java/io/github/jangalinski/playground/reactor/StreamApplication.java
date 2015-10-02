package io.github.jangalinski.playground.reactor;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.Environment;
import reactor.fn.Consumer;
import reactor.fn.Supplier;
import reactor.rx.Streams;
import reactor.spring.context.config.EnableReactor;

import javax.annotation.PostConstruct;
import java.util.Random;

import static org.slf4j.LoggerFactory.getLogger;

@SpringBootApplication
@EnableScheduling
public class StreamApplication implements CommandLineRunner {

  private static final Logger logger = getLogger(StreamApplication.class);

  public static void main(String... args) {
    SpringApplication.run(StreamApplication.class, args);
  }

  @Autowired
  private RandomSupplier r;

  @Autowired
  private LoggingConsumer c;

  @Override
  public void run(String... strings) throws Exception {
    Streams.just("a", "b").consume(c);
    while(true) {
      Streams.generate(r).dispatchOn(Environment.sharedDispatcher()).consume(c);
    }


  }

  @Component
  public static class LoggingConsumer implements Consumer<Object> {

    @Override
    public void accept(Object o) {
      logger.info("{} - {}", Thread.currentThread(), o);
    }
  }

  @Component
  public static class RandomSupplier implements Supplier<Integer> {

    private final Random random = new Random();

    //@Scheduled(fixedDelay = 1000L)
    //public void scheduledGet() {
//      logger.info("r={}",get());
  //  }

    @Override
    public Integer get() {
      try {
        Thread.currentThread().wait(1000L);
      } catch (InterruptedException e) {
        //
      }
      return random.nextInt(1000);
    }

  }


  @Configuration
  @EnableAutoConfiguration
  @EnableReactor
  public static class ReactorConfiguration {

    @PostConstruct
    public void init() {
      Environment.initializeIfEmpty().assignErrorJournal();
    }
  }
}
