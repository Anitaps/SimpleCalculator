-module(server).

-behaviour(gen_server).

-import(string,[substr/3]).

-define(SERVER, ?MODULE).

%% ------------------------------------------------------------------
%% API Function Exports
%% ------------------------------------------------------------------

-export([start_link/0, stop/0]).

%% ------------------------------------------------------------------
%% gen_server Function Exports
%% ------------------------------------------------------------------

-export([init/1, handle_call/3, handle_cast/2, handle_info/2, calculate/3,
         terminate/2, code_change/3]).

-record(state, {mqttc}).
%% ------------------------------------------------------------------
%% API Function Definitions
%% ------------------------------------------------------------------

start_link() ->
    gen_server:start_link({local, ?SERVER}, ?MODULE, [], []).

stop() ->
    gen_server:call(?SERVER, stop).

%% ------------------------------------------------------------------
%% gen_server Function Definitions
%% ------------------------------------------------------------------
%initialize function taking in some arguments and it sets the client /C is the client
init(_Args) ->
    {ok, C} = emqttc:start_link([{host, "prata.technocreatives.com"}, 
                                 {client_id, <<"Anitaclient">>},
                                 {reconnect, 3},
                                 {logger, {console, info}}]),
    %% The pending subscribe
    emqttc:subscribe(C, <<"Mylovely/calculator/AndroidMessage">>, qos1),
    {ok, #state{mqttc = C}}.

handle_call(stop, _From, State) ->
    {stop, normal, ok, State};

handle_call(_Request, _From, State) ->
    {reply, ok, State}.

handle_cast(_Msg, State) ->
    {noreply, State}.

%% Receive Messages from broker
handle_info({publish, Topic, Payload}, State = #state{mqttc = C}) ->
    io:format("Message from ~s: ~p~n", [Topic, Payload]),
   
    Msg = binary:bin_to_list(Payload),
   
    P = substr(Msg,1,1),
    N = list_to_integer(P),
   
    Operat = substr(Msg,2,1),
   
    R = substr(Msg,3,1),
    M = list_to_integer(R),

    Carrier = calculate(N,Operat,M),
    io:format([Carrier]),

    case Carrier of
    
    "Undefined" ->
    PayloadB = list_to_binary(["Result: ", Carrier]),
    emqttc:publish(C, <<"Mylovely/calculator/Timer">>, PayloadB, qos1);
    
    _Else ->
    PayloadB = list_to_binary(["Result: ", integer_to_list(Carrier)]),
    emqttc:publish(C, <<"Mylovely/calculator/Timer">>, PayloadB, qos1)
    end,
    {noreply, State};
   

%% Client connected
handle_info({mqttc, C, connected}, State = #state{mqttc = C}) ->
    io:format("Client ~p is connected~n", [C]),
    emqttc:subscribe(C, <<"Mylovely/calculator/AndroidMessage">>, qos1),
    self() ! publish,
    {noreply, State};

%% Client disconnected
handle_info({mqttc, C,  disconnected}, State = #state{mqttc = C}) ->
    io:format("Client ~p is disconnected~n", [C]),
    {noreply, State};

handle_info(_Info, State) ->
    {noreply, State}.

calculate(N, "+" , M) -> N+M;
calculate(N, "*" , M) -> N*M;
calculate(N, "-" , M) -> N-M;
calculate(_N, "/" , M) when M == 0 -> "Undefined";
calculate(N, "/" , M) -> N div M.

terminate(_Reason, _State) ->
    ok.

code_change(_OldVsn, State, _Extra) ->
    {ok, State}.

