package ru.bmstu.BankBillingServer;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.HttpApp;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.unmarshalling.StringUnmarshallers;
import akka.pattern.PatternsCS;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import ru.bmstu.BankBillingServer.AccountRegistration.AccountMessage;
import ru.bmstu.BankBillingServer.AccountRegistration.AccountRegistrar;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import static akka.http.javadsl.server.Directives.*;
import static akka.http.javadsl.server.PathMatchers.integerSegment;

public class Server {
    private static final int PORT = 9696;
    private static final String HOST = "localhost";

    private ActorRef accountRegistrar;
    private static final String ACCOUNT_REGISTRAR_ACTOR = "accountRegistrar";

    public static void main(String[] args) throws IOException {
        Logger log = Logger.getLogger("Logger");
        ActorSystem system = ActorSystem.create("routes");

        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        Server app = new Server(system);

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.createRoute().flow(system, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(
                routeFlow,
                ConnectHttp.toHost(HOST, PORT),
                materializer
        );
        log.info("Server is started at " + HOST + ":" + PORT);

        System.in.read();

        binding
                .thenCompose(ServerBinding::unbind)
                .thenAccept(unbound -> system.terminate());

        log.info("Server is stopped");
    }

    private Server(final ActorSystem system) {
        this.accountRegistrar = system.actorOf(Props.create(AccountRegistrar.class), ACCOUNT_REGISTRAR_ACTOR);
    }

    private Route createRoute() {
        return route(
                post(() ->
                    pathPrefix("bankaccount", () ->
                        path(integerSegment(), (Integer id) -> {
                            CompletableFuture<Object> result = PatternsCS.ask(
                                    accountRegistrar,
                                    new AccountMessage(id),
                                    5000
                            ).toCompletableFuture();

                            return completeOKWithFuture(result, Jackson.marshaller());
                        })
                    )
                ));
    }
}
