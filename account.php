<?php

error_reporting(E_ALL);
ini_set('display_errors', '1');
ini_set('display_startup_errors', '1');

$home = getenv("HOME");
$dbPath = "$home/Databases/wse stocks.db";
// $default_balance = 1000.0;

$user_id = $_GET['user_id'] ?? null;
$action = $_GET['action'] ?? null;
$start_date = $_GET['start_date'] ?? null;
$default_balance = $_GET['default_balance'] ?? null;
$amount = $_GET['amount'] ?? null;
$date_to_switch = $_GET['date'] ?? null;

$errMessage = ""; // Пустая строка для описания ошибки соединения с БД


if ($action == "create_new_account") {
    $funResault = createNewAccount($dbPath);
    if ($funResault) {
        global $user_id;
            $response = [
                'status' => 'Ok',
                'message' => $user_id,
            ];
            echo json_encode($response);
            exit;
    } elseif (!$funResault) {
            $response = [
                'status' => 'Error',
                'message' => "Broker account.php Error create new account. " . $errMessage,
            ];
            echo json_encode($response);
            exit;
    }
}
if ($action == "get_balance") {
    $balance = getBalance($user_id);
    if ($balance) {
        $response = [
            'status' => 'Ok',
            'message' => $balance,
        ];
        echo json_encode($response);
        exit;
    } elseif ($balance == false) {
        $response = [
            'status' => 'Error',
            'message' => "Broker account.php Error get balance. " . $errMessage,
        ];
        echo json_encode($response);
        exit;
    }
}
    
if ($action == "set_balance" and $amount) {
    $balance = setBalance($user_id, $amount);
    if ($balance) {
        $response = [
            'status' => 'Ok',
            'message' => 'Balance was set successfully.',
        ];
        echo json_encode($response);
        exit;
    } elseif ($balance == false) {
        $response = [
            'status' => 'Error',
            'message' => 'Balance is not set. ' . $errMessage,
        ];
        echo json_encode($response);
        exit;
            
    }
}

// Когда сюда приходит запрос нам нужно выполнить все текущие ордера
// А если ордер не выполнен тогда записать их обратно в БД
if ($action == "switch_to_date") {
    $remainingOrders = [];

    $orders = getOrders($user_id);

    foreach ($orders as $order) {
        $result = executeOrder($order);

        // Если ордер не выполнен возвращаем его в очередь.
        if (!$result) {
            $remainingOrders[] = $order;
            saveOrder($order);
        } 
    }
    $result = setDate($user_id, $date_to_switch);
    if ($result) {
        $response = json_encode(['status' => 'Ok', 'message' => 'Broker, account. The date is changed.']);
        echo $response;
    } else {
        $response = json_encode(['status' => 'Error', 'message' => 'Broker, account. The date is not changed.']);
        echo $response;
    }
    exit;
}

function createNewAccount($dbPath) {
    global $user_id, $start_date, $default_balance;
    $user_id = bin2hex(random_bytes(16));

    $portfolio_table_name = 'portfolio_' . $user_id;
    $balance_table_name = 'balance_' . $user_id;
    $date_table_name = 'date_' . $user_id;
    $orders_table_name = 'orders_' . $user_id;


    try {
        // Подключение к SQLite
        $pdo = new PDO("sqlite:$dbPath");
        $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

        // SQL для таблицы портфеля
        $create_portfolio = "
            CREATE TABLE IF NOT EXISTS $portfolio_table_name (
                ticker TEXT PRIMARY KEY,
                quantity INTEGER NOT NULL
            );
        ";

        // SQL для таблицы баланса
        $create_balance = "
            CREATE TABLE IF NOT EXISTS $balance_table_name (
                balance REAL NOT NULL
            );
        ";

        $create_date = "
            CREATE TABLE IF NOT EXISTS $date_table_name (
                date TEXT NOT NULL
            );
        ";

        $create_orders = "
            CREATE TABLE IF NOT EXISTS $orders_table_name (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                ticker TEXT NOT NULL,
                order_side TEXT NOT NULL,
                order_type TEXT NOT NULL,
                quantity INTEGER NOT NULL,
                price REAL NOT NULL,
                user_id TEXT NOT NULL,
                created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            );
        ";

        // Выполнение запросов
        $pdo->exec($create_portfolio);
        $pdo->exec($create_balance);
        $pdo->exec($create_date);
        $pdo->exec($create_orders);
        
        $stmt = $pdo->query("SELECT COUNT(*) FROM $balance_table_name");
        $count = $stmt->fetchColumn();

        // Если таблица пуста — вставляем значение по умолчанию для balance
        if ($count == 0) {
            $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
            $stmt = $pdo->prepare("INSERT INTO $balance_table_name (balance) VALUES (:amount)");
            $stmt->execute([':amount' => $default_balance]);
        }

        $stmt = $pdo->query("SELECT COUNT(*) FROM $date_table_name");
        $date = $stmt->fetchColumn();
        // Если таблица пуста — вставляем значение по умолчанию для date
        if ($date == 0) {
            $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
            $stmt = $pdo->prepare("INSERT INTO $date_table_name (date) VALUES (:date)");
            $stmt->execute([':date' => $start_date]);
        }
        return true;
    } catch (PDOException $e) {
            global $errMessage;
            $errMessage = $e -> getMessage();
            return false;
    }
}

function getBalance($user_id) {
    global $dbPath;
    $balance_table_name = 'balance_' . $user_id;

    try {
        $db = new PDO("sqlite:$dbPath");
        $stmt = $db->query("SELECT balance FROM $balance_table_name LIMIT 1");
        $row = $stmt->fetch(PDO::FETCH_ASSOC);

        if ($row && isset($row["balance"])) {
            return $row["balance"];
        } else {
            return false;
        }
    } catch (PDOException $e) {
        global $errMessage;
        $errMessage = $e->getMessage();
        return false;
    }


}

function setBalance($user_id, $amount) {
    global $dbPath;
    $balance_table_name = 'balance_' . $user_id;

    try {
        // Подключение к базе
        $pdo = new PDO("sqlite:$dbPath");
        $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

        // SQL-запрос на вставку
        $stmt = $pdo->prepare("UPDATE $balance_table_name SET balance = :amount");
        $stmt->execute([':amount' => $amount]);
        return true;
    } catch (PDOException $e) {
        global $errMessage;
        $errMessage = $e->getMessage();
        return false;
    }

}

function saveOrder(array $order) {
    global $dbPath;

    try {
        // Проверка обязательных параметров
        $required = ['ticker', 'order_side', 'order_type', 'quantity', 'price', 'user_id'];
        foreach ($required as $key) {
            if (empty($order[$key])) {
                return ['status' => 'Error', 'message' => "Missing parameter: $key"];
            }
        }

        $orders_table_name = "orders_" . $order['user_id'];

        // Подключение к базе
        $pdo = new PDO("sqlite:$dbPath");
        $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

        // Вставка ордера
        $stmt = $pdo->prepare("
            INSERT INTO $orders_table_name (ticker, order_side, order_type, quantity, price, user_id)
            VALUES (:ticker, :side, :order_type, :quantity, :price, :user_id)
        ");

        $stmt->execute([
            ':ticker'     => $order['ticker'],
            ':side'       => $order['order_side'],
            ':order_type' => $order['order_type'],
            ':quantity'   => (int)$order['quantity'],
            ':price'      => (float)$order['price'],
            ':user_id'    => $order['user_id'],
        ]);

        return json_encode(['status' => 'Ok', 'message' => 'Broker account.php. Order saved.']);

    } catch (PDOException $e) {
        return ['status' => 'Error', 'message' => $e->getMessage()];
    }
}


function getOrders($user_id) {
    global $dbPath;

    $orders_table_name = "orders_" . $user_id;

    try {
        $pdo = new PDO("sqlite:$dbPath");
        $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

        // Получаем все ордера
        $stmt = $pdo->query("SELECT * FROM $orders_table_name");
        $orders = $stmt->fetchAll(PDO::FETCH_ASSOC);

        // Удаляем все ордера из таблицы
        $pdo->exec("DELETE FROM $orders_table_name");

        return $orders;

    } catch (PDOException $e) {
        return ['status' => 'Error', 'message' => $e->getMessage()];
    }
}


function executeOrder($order) {

    if ($order["order_side"] == "BUY") {
        return buyShares($order);
    } elseif ($order["order_side"] == "SELL") {
        return sellShares($order);
    }
}

function buyShares($order) {
    // Перед тем как сделать виртуальную покупку нужно проверить соответствует ли биржевая цена заданой сесии цене ордера.
    $user_id = $order["user_id"];
    $price = (float)$order["price"];
    $quantity = (int)$order['quantity'];
    $ticker_name = $order["ticker"];
    $current_date = getCurrentDate($user_id);

    $url = "http://market.local/?ticker=".$ticker_name."&date=".$current_date;
    $market_data = file_get_contents($url);
    $response = json_decode($market_data, true);

    if ($response['status'] == "Ok") {

        $low = (float)$response['low'];
        $high = (float)$response['high'];
        if ($low <= $price) {
            $total_cost = round($price * $quantity + calcCommission($price, $quantity), 3, PHP_ROUND_HALF_EVEN);
            // Отнимаем $total_cost от баланса
            $current_balance = (float)getBalance($user_id);
            $new_balance = round($current_balance - $total_cost, 3, PHP_ROUND_HALF_EVEN);
            setBalance($user_id, $new_balance);
            // Добавляем акции на счёт клиента
            $curent_shares_on_db = getShares($user_id, $ticker_name);
            $new_quantity = $curent_shares_on_db + $quantity;
            setShares($user_id, $ticker_name, $new_quantity);
            // echo "$current_date. Bought $ticker_name $quantity * $price total cost: $total_cost. Balance: $new_balance \n";
            return true;
        }
        
    } elseif ($response['status'] == "Mistake") {
        return false;
    }
}

function sellShares($order) {
    // Перед тем как сделать виртуальную продажу нужно проверить соответствует ли биржевая цена заданой сесии цене ордера.
    $user_id = $order["user_id"];
    $price = (float)$order["price"];
    $quantity = (int)$order['quantity'];
    $ticker_name = $order["ticker"];
    $current_date = getCurrentDate($user_id);

    $url = "http://market.local/?ticker=".$ticker_name."&date=".$current_date;
    $market_data = file_get_contents($url);
    $response = json_decode($market_data, true);

    if ($response['status'] == "Ok") {

        // $low = (float)$response['low'];
        $high = (float)$response['high'];
        if ($price <= $high) {
            $total_cost = round($price * $quantity - calcCommission($price, $quantity), 3, PHP_ROUND_HALF_EVEN);
            // Отнимаем $total_cost от баланса
            $current_balance = (float)getBalance($user_id);
            $new_balance = round($current_balance + $total_cost, 3, PHP_ROUND_HALF_EVEN);
            setBalance($user_id, $new_balance);
            // Добавляем акции на счёт клиента
            $curent_shares_on_db = getShares($user_id, $ticker_name);
            $new_quantity = $curent_shares_on_db - $quantity;
            setShares($user_id, $ticker_name, $new_quantity);
            // echo "$current_date. Sold $ticker_name $quantity * $price total cost: $total_cost. Balance: $new_balance \n";
            return true;
        }
        
    } elseif ($response['status'] == "Mistake") {
        return false;
    }
}

function calcCommission($price, $quantity) {
    $min_commission = 5.0;
    $commission = $price * $quantity / 100 * 0.39;
    $commission = round($commission, 3, PHP_ROUND_HALF_EVEN);

    if ($commission > $min_commission) {
        return $commission;
    } else {
        return $min_commission;
    }
}

function getCurrentDate($user_id) {
    global $dbPath;
    try {
        $date_table_name = 'date_' . $user_id;

        $pdo = new PDO("sqlite:$dbPath");
        $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

        $stmt = $pdo->query("SELECT date FROM $date_table_name LIMIT 1");
        $result = $stmt->fetch(PDO::FETCH_ASSOC);

        if ($result && isset($result['date'])) {
            return $result['date'];
        } else {
            return null; // или можно вернуть дефолтную дату
        }

    } catch (PDOException $e) {
        global $errMessage;
        $errMessage = $e->getMessage();
        return null;
    }
}

function setDate($user_id, $date) {
    global $dbPath;
    try {
        $date_table_name = 'date_' . $user_id;

        $pdo = new PDO("sqlite:$dbPath");
        $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

        // Проверяем, есть ли уже строка
        $stmt = $pdo->query("SELECT COUNT(*) FROM $date_table_name");
        $count = $stmt->fetchColumn();

        if ($count > 0) {
            // Обновляем существующую дату
            $stmt = $pdo->prepare("UPDATE $date_table_name SET date = :date");
        } else {
            // Вставляем новую дату
            $stmt = $pdo->prepare("INSERT INTO $date_table_name (date) VALUES (:date)");
        }

        $stmt->execute([':date' => $date]);
        // echo "Current date: $date \n";
        return true;

    } catch (PDOException $e) {
        // global $errMessage;
        // $errMessage = $e->getMessage();
        return false;
    }
}

function getShares($user_id, $ticker){
    global $dbPath;

    $portfolio_table_name = 'portfolio_' . $user_id;

    try {
        $pdo = new PDO("sqlite:$dbPath");
        $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

        $stmt = $pdo->prepare("SELECT quantity FROM $portfolio_table_name WHERE ticker = :ticker");
        $stmt->execute([':ticker' => $ticker]);

        $result = $stmt->fetch(PDO::FETCH_ASSOC);
        if ($result === false) {
            return 0;  // Акция не найдена — считаем 0
        }
        return (int)$result['quantity'];
    } catch (PDOException $e) {
        // Можно логировать ошибку или возвращать 0
        return 0;
    }
}

function setShares($user_id, $ticker, $quantity) {
    global $dbPath;

    $portfolio_table_name = 'portfolio_' . $user_id;

    try {
        $pdo = new PDO("sqlite:$dbPath");
        $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

        // Проверим, есть ли запись по тикеру
        $stmt = $pdo->prepare("SELECT COUNT(*) FROM $portfolio_table_name WHERE ticker = :ticker");
        $stmt->execute([':ticker' => $ticker]);
        $exists = $stmt->fetchColumn() > 0;

        if ($exists) {
            // Обновляем количество
            $stmt = $pdo->prepare("UPDATE $portfolio_table_name SET quantity = :quantity WHERE ticker = :ticker");
            $stmt->execute([
                ':quantity' => $quantity,
                ':ticker' => $ticker
            ]);
        } else {
            // Вставляем новую запись
            $stmt = $pdo->prepare("INSERT INTO $portfolio_table_name (ticker, quantity) VALUES (:ticker, :quantity)");
            $stmt->execute([
                ':ticker' => $ticker,
                ':quantity' => $quantity
            ]);
        }

        return true;
    } catch (PDOException $e) {
        // Логируем ошибку, можно вернуть false
        return false;
    }

}

function getMarketData($ticker, $date) {
    $url = "http://market.local/?ticker=$ticker&date=$date";
    $response = file_get_contents($url);
    return $response;
}


// http://broker.local/account?action=create_new_account&start_date=01-01-2008
// http://broker.local/account?user_id=a4df4d5e6c5b5bac925f8c373977ab79&action=get_balance
// http://broker.local/account?user_id=5699693c2386a98742ca8d70dff72173&action=get_balance
// http://broker.local/account?user_id=a4df4d5e6c5b5bac925f8c373977ab79&action=set_balance&amount=11111