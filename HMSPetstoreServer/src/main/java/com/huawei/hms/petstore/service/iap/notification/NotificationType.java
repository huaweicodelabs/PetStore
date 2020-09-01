/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.huawei.hms.petstore.service.iap.notification;

/**
 * 功能描述
 *
 * @author iap
 * @since 2020-02-05
 */
public interface NotificationType {

    int INITIAL_BUY = 0;

    int CANCEL = 1;

    int RENEWAL = 2;

    int INTERACTIVE_RENEWAL = 3;

    int NEW_RENEWAL_PREF = 4;

    int RENEWAL_STOPPED = 5;

    int RENEWAL_RESTORED = 6;

    int RENEWAL_RECURRING = 7;

    int ON_HOLD = 9;

    int PAUSED = 10;

    int PAUSE_PLAN_CHANGED = 11;

    int PRICE_CHANGE_CONFIRMED = 12;

    int DEFERRED = 13;
}
