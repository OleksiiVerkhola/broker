<?php

error_reporting(E_ALL);
ini_set('display_errors', '1');
ini_set('display_startup_errors', '1');

require_once 'account.php';
require_once 'Order.php';

// Получаем и обрабатываем запрос
// От пользователя может прийти несколько разных запросов касающихся счёта (его создание или получение данных) или может прийти order

// GTC (Good Till Cancelled) — ордер действует до отмены.
// IOC (Immediate Or Cancel) — ордер должен быть выполнен сразу или отменён.
// FOK (Fill Or Kill) — ордер либо исполняется полностью сразу, либо отменяется.
// GTD (Good Till Date) — ордер действует до определённой даты.
// OPG — At the opening (исполнить по цене открытия сессии)
// CLS — At the close (исполнить по цене закрытия сессии)

// http://broker.local/?ticker=ENA&order_type=buy&price=100&quantity=10&time_in_force=cls

$ticker = $_GET['ticker'] ?? null;
$order_side = $_GET['order_side'] ?? null;
$order_type = $_GET['order_type'] ?? null;
$quantity = $_GET['quantity'] ?? null;
$price = $_GET['price'] ?? null;
$user_id = $_GET['user_id'] ?? null;

// echo $_SERVER["QUERY_STRING"];
// echo $_GET['order_type'];

// Первостепенная проверка входных параметров от Client
if (!$ticker || !$order_side || !$order_type || !$quantity || !$price || !$user_id) {
    echo json_encode(['status' => 'Error', 'message' => 'Broker. Bad params in the request from Client.']);
    exit;
} else {
    // Немедленно исполняем
    if ($order_type == "OPG") {
        if ($order_side == "BUY") {
            $order = [];
            $market_data = getMarketData($ticker, getCurrentDate($user_id));
            $market_data = json_decode($market_data, true);

            parse_str($_SERVER['QUERY_STRING'], $order);
            if ($market_data['status'] == "Ok") {
                $order['price'] = $market_data['open'];
                $result_of_buy = buyShares($order);
            } elseif ($market_data['status'] == 'Warning') {
                $result_of_buy = false;
            }

            // echo "Resault of buy is " . $result_of_buy;

            if ($result_of_buy) {
                // echo "Response is work";
                $response = json_encode(['status' => 'Ok', 'message' => 'Broker. Order OPG BUY is executed.', 'price' => $market_data['open']]);
                echo $response;
                exit;
            } else {
                $response = json_encode(['status' => 'Warning', 'message' => 'Broker. Order for OPG BUY is rejected.']);
                echo $response;
                exit;
            }
        }
        if ($order_side == "SELL") {
            $order = [];
            parse_str($_SERVER['QUERY_STRING'], $order);
            // sellShares($order);
        }
    } else {
        // Функция из файла account.php
        $order = [];
        parse_str($_SERVER['QUERY_STRING'], $order);

        $resault_of_save = saveOrder($order);
        var_dump($resault_of_save);
        // echo $resault_of_save;
        $response = json_encode(['status' => 'Ok', 'message' => 'Broker. Order OPG SELL is executed.']);
        exit;
    }
    // echo json_encode(['status' => 'Ok', 'message' => 'Broker. The order is accepted.']);
}








// URL API сервера
// $url = "http://market.local/?ticker=$ticker&date=$date";


function sendRequestToMarket($request_url) {
    $ch = curl_init($request_url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    $response = curl_exec($ch);

    if ($response === false) {
        $response = json_encode(['status' => 'Error', 'message' => 'Broker. Market. Error.']);
        echo $response;
        curl_close($ch);
        exit;
    }

    curl_close($ch);

    // Декодируем JSON ответ
    $data = json_decode($response, true);

    if (json_last_error() !== JSON_ERROR_NONE) {
        echo "Ошибка декодирования JSON: " . json_last_error_msg();
        exit;
    }

    // Обработка результата
    if ($data['status'] == "Ok") {
        // echo "Данные получены:\n";
        var_dump($data);
    } elseif($data['status'] == "Mistake") {
        // echo "Mistake: " . $data['message'];
    } elseif($data['status'] == "Error") {
        // echo "Error: " . $data['message'];
    }
}