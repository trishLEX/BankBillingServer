package ru.bmstu.BankBillingServer;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCode;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import akka.pattern.PatternsCS;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import ru.bmstu.BankBillingServer.Delete.DeleteAccountMessage;
import ru.bmstu.BankBillingServer.Delete.DeleteActor;
import ru.bmstu.BankBillingServer.Get.GetActor;
import ru.bmstu.BankBillingServer.Get.GetBalanceMessage;
import ru.bmstu.BankBillingServer.Post.CreateAccountMessage;
import ru.bmstu.BankBillingServer.Post.PostActor;
import ru.bmstu.BankBillingServer.Put.DepositMessage;
import ru.bmstu.BankBillingServer.Put.PutActor;
import ru.bmstu.BankBillingServer.Put.WithdrawMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import static akka.http.javadsl.server.Directives.*;
import static akka.http.javadsl.server.PathMatchers.integerSegment;

public class Server {
    private static final int PORT = 9696;
    private static final String HOST = "localhost";

    private static final int TIMEOUT_MILLIS = 5000;

    private ActorRef getActor;
    private static final String GET_ACTOR = "getActor";

    private ActorRef postActor;
    private static final String POST_ACTOR = "postActor";

    private ActorRef putActor;
    private static final String PUT_ACTOR = "putActor";

    private ActorRef deleteActor;
    private static final String DELETE_ACTOR = "deleteActor";

    private static final String ACCOUNTS = "accounts";

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
        this.getActor = system.actorOf(Props.create(GetActor.class), GET_ACTOR);
        this.postActor = system.actorOf(Props.create(PostActor.class), POST_ACTOR);
        this.putActor = system.actorOf(Props.create(PutActor.class), PUT_ACTOR);
        this.deleteActor = system.actorOf(Props.create(DeleteActor.class), DELETE_ACTOR);
    }

    private Route createRoute() {
        return route(
                post(() ->
                        parameter("id", accountID ->
                                pathPrefix("bankaccount", () ->
                                        createAccountRoute(Integer.parseInt(accountID))
                                )
                        )
                ),
                put(() ->
                        parameter("id", accountID ->
                                parameter("sum", sum ->
                                        pathPrefix("bankaccount", () ->
                                                accountManagementRoute(Integer.parseInt(accountID), Float.parseFloat(sum))
                                        )
                                )
                        )
                ),
                get(() -> route(
                        parameter("id", accountID ->
                                pathPrefix("bankaccount", () ->
                                        getBalanceRoute(Integer.parseInt(accountID))
                                )
                        ),
                        path("bankaccount", () ->
                                        getBalanceRoute()
                                )
                        )
                ),
                delete(() ->
                        parameter("id", accountID ->
                                pathPrefix("bankaccount", () ->
                                        deleteRoute(Integer.parseInt(accountID))
                                )
                        )
                )
        );
    }

    private Route createAccountRoute(int accountID) {
        return path(integerSegment(), (Integer id) -> {
            if (accountID != id)
                return complete(StatusCodes.BAD_REQUEST, "wrong id");
            else {
                CompletableFuture<StatusCode> result = PatternsCS.ask(
                        postActor,
                        new CreateAccountMessage(id), TIMEOUT_MILLIS)
                        .toCompletableFuture()
                        .thenApply((StatusCode.class::cast));
                try {
                    return complete(result.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    return complete(StatusCodes.NOT_ACCEPTABLE);
                }
            }
        });
    }

    private Route accountManagementRoute(int accountID, float sum) {
        return pathPrefix(integerSegment(), (Integer id) ->
                route(
                        depositRoute(accountID, id, sum),
                        withdrawRoute(accountID, id, sum)
                )
        );
    }

    private Route depositRoute(int accountID, int id, float sum) {
        return path("deposit", () -> {
            if (accountID != id)
                return complete(StatusCodes.BAD_REQUEST, "wrong id");
            else {
                CompletableFuture<StatusCode> result = PatternsCS.ask(
                        putActor,
                        new DepositMessage(id, sum), TIMEOUT_MILLIS)
                        .toCompletableFuture()
                        .thenApply((StatusCode.class::cast));
                try {
                    return complete(result.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    return complete(StatusCodes.NOT_ACCEPTABLE);
                }
            }
        });
    }

    private Route withdrawRoute(int accountID, int id, float sum) {
        return path("withdraw", () -> {
            if (accountID != id)
                return complete(StatusCodes.BAD_REQUEST, "wrong id");
            else {
                CompletableFuture<StatusCode> result = PatternsCS.ask(
                        putActor,
                        new WithdrawMessage(id, sum), TIMEOUT_MILLIS)
                        .toCompletableFuture()
                        .thenApply((StatusCode.class::cast));
                try {
                    return complete(result.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    return complete(StatusCodes.NOT_ACCEPTABLE);
                }
            }
        });
    }

    private Route getBalanceRoute(int accountID) {
        return pathPrefix(integerSegment(), (Integer id) ->
                path("balance", () -> {
                    if (accountID != id)
                        return complete(StatusCodes.BAD_REQUEST, "wrong id");
                    else {
                        CompletableFuture<Double> result = PatternsCS.ask(
                                getActor,
                                new GetBalanceMessage(id), TIMEOUT_MILLIS)
                                .toCompletableFuture()
                                .thenApply((Double.class::cast));
                        try {
                            Double balance = result.get();
                            if (balance < 0)
                                return complete(StatusCodes.BAD_REQUEST);
                            else
                                return complete(balance.toString());
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                            return complete(StatusCodes.NOT_ACCEPTABLE);
                        }
                    }
                })
        );
    }

    private Route getBalanceRoute() {
        CompletableFuture<HashMap<Integer, Double>> result = PatternsCS.ask(getActor, ACCOUNTS, TIMEOUT_MILLIS)
                .toCompletableFuture()
                .thenApply((HashMap.class::cast));

        return completeOKWithFuture(result, Jackson.marshaller());
    }

    private Route deleteRoute(int accountID) {
        return pathPrefix(integerSegment(), (Integer id) ->
                path("delete", () -> {
                    if (accountID != id)
                        return complete(StatusCodes.BAD_REQUEST, "wrong id");
                    else {
                        CompletableFuture<StatusCode> result = PatternsCS.ask(
                                deleteActor,
                                new DeleteAccountMessage(id), TIMEOUT_MILLIS)
                                .toCompletableFuture()
                                .thenApply((StatusCode.class::cast));
                        try {
                            return complete(result.get());
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                            return complete(StatusCodes.NOT_ACCEPTABLE);
                        }
                    }
                })
        );
    }
}
