package org.broker.classes;

import java.util.UUID;
/*
*  Генерирует уникальный id для userId
* */
public class Security {
    public String generateUniqueString() {
        // Убираю дефисы из строки потому что sql на них ругается.
        return UUID.randomUUID().toString().replace("-", "");
    }
}
