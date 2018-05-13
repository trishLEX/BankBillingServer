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
import akka.http.javadsl.model.StatusCode;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import akka.pattern.PatternsCS;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import ru.bmstu.BankBillingServer.AccountManagement.*;
import ru.bmstu.BankBillingServer.BillingDB.BillingDB;

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

    private static final int TIMEOUT_MILLIS = 5000;

    private ActorRef billingDB;
    private static final String BILLING_DB_ACTOR = "billingDB";

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
        this.billingDB = system.actorOf(Props.create(BillingDB.class), BILLING_DB_ACTOR);
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
                get(() ->
                        parameter("id", accountID ->
                                pathPrefix("bankaccount", () ->
                                        getBalanceRoute(Integer.parseInt(accountID))
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
                        billingDB,
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
                        billingDB,
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
                        billingDB,
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
                                billingDB,
                                new GetBalanceMessage(id), TIMEOUT_MILLIS)
                                .toCompletableFuture()
                                .thenApply((Double.class::cast));
                        try {
                            Double balance = result.get();
                            if (balance == null)
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

    private Route deleteRoute(int accountID) {
        return pathPrefix(integerSegment(), (Integer id) ->
                path("delete", () -> {
                    if (accountID != id)
                        return complete(StatusCodes.BAD_REQUEST, "wrong id");
                    else {
                        CompletableFuture<StatusCode> result = PatternsCS.ask(
                                billingDB,
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
