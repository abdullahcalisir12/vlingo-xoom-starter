// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.starter.restapi.data;

import java.util.ArrayList;
import java.util.List;

public class ModelSettingsData {

    public final PersistenceData persistenceSettings;
    public final List<AggregateData> aggregateSettings = new ArrayList<>();

    public ModelSettingsData(final PersistenceData persistenceSettings,
                             final List<AggregateData> aggregateSettings) {
        this.persistenceSettings = persistenceSettings;
        this.aggregateSettings.addAll(aggregateSettings);
    }

    public List<String> validate(List<String> errorStrings) {
        if(persistenceSettings==null) {
            errorStrings.add("ModelSettingsData.persistence is null");
        } else {
            persistenceSettings.validate(errorStrings);
        }
        if(aggregateSettings==null) {
            errorStrings.add("ModelSettingsData.aggregateSettings is null");
        } else {
            aggregateSettings.forEach(aggregateSetting ->
                aggregateSetting.validate(errorStrings)
            );
        }
        return errorStrings;
    }
}
