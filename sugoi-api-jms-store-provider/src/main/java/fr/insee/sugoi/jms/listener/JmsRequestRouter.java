/*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package fr.insee.sugoi.jms.listener;

import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.jms.model.BrokerRequest;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.UserStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JmsRequestRouter {

  private static final Logger logger = LogManager.getLogger(JmsReceiverRequest.class);

  @Autowired private StoreProvider storeProvider;

  @Autowired private RealmProvider realmProvider;

  public void exec(BrokerRequest request) {
    logger.info(
        "Receive request from Broker for realm {} operation:{}",
        request.getmethodParams().get("domain"),
        request.getMethod());
    Realm realm = realmProvider.load((String) request.getmethodParams().get("domain"));
    UserStorage userStorage = realm.getUserStorages().get(0);
    switch (request.getMethod()) {
      case "deleteUser":
        storeProvider
            .getStoreForUserStorage(realm.getName(), userStorage.getName())
            .getWriter()
            .deleteUser("id");
        break;

      default:
        break;
    }
  }
}