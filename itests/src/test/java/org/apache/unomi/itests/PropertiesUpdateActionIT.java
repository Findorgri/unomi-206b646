/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package org.apache.unomi.itests;

import org.apache.unomi.api.Event;
import org.apache.unomi.api.Profile;
import org.apache.unomi.api.services.EventService;
import org.apache.unomi.api.services.ProfileService;
import org.apache.unomi.plugins.baseplugin.actions.UpdatePropertiesAction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.ops4j.pax.exam.util.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by amidani on 12/10/2017.
 */

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class PropertiesUpdateActionIT extends BaseIT {
    private final static Logger LOGGER = LoggerFactory.getLogger(PropertiesUpdateActionIT.class);

    private final static String PROFILE_TARGET_TEST_ID = "profile-target-event";
    private final static String PROFILE_TEST_ID = "profile-to-update-by-event";

    @Inject @Filter(timeout = 600000)
    protected ProfileService profileService;
    @Inject @Filter(timeout = 600000)
    protected EventService eventService;

    @Before
    public void setUp() throws IOException {
        Profile profile = new Profile();
        profile.setItemId(PROFILE_TEST_ID);
        profileService.save(profile);
        LOGGER.info("Profile saved with ID [{}].", profile.getItemId());

        Profile profileTarget = new Profile();
        profileTarget.setItemId(PROFILE_TARGET_TEST_ID);
        profileService.save(profileTarget);
        LOGGER.info("Profile saved with ID [{}].", profileTarget.getItemId());
    }

    @Test
    public void testUpdateProperties_CurrentProfile() {
        Profile profile = profileService.load(PROFILE_TARGET_TEST_ID);
        Assert.assertNull(profile.getProperty("firstName"));

        Event updateProperties = new Event("updateProperties", null, profile, null, null, profile, new Date());
        updateProperties.setPersistent(false);

        Map<String, Object> propertyToUpdate = new HashMap<>();
        propertyToUpdate.put("properties.firstName", "UPDATED FIRST NAME CURRENT PROFILE");

        updateProperties.setProperty(UpdatePropertiesAction.PROPS_TO_UPDATE, propertyToUpdate);
        int changes = eventService.send(updateProperties);

        LOGGER.info("Changes of the event : {}", changes);

        Assert.assertTrue(changes > 0);
    }

    @Test
    public void testUpdateProperties_NotCurrentProfile() {

        Profile profile = profileService.load(PROFILE_TARGET_TEST_ID);
        Profile profileToUpdate = profileService.load(PROFILE_TEST_ID);
        Assert.assertNull(profileToUpdate.getProperty("firstName"));

        Event updateProperties = new Event("updateProperties", null, profile, null, null, profile, new Date());
        updateProperties.setPersistent(false);

        Map<String, Object> propertyToUpdate = new HashMap<>();
        propertyToUpdate.put("properties.firstName", "UPDATED FIRST NAME");

        updateProperties.setProperty(UpdatePropertiesAction.PROPS_TO_UPDATE, propertyToUpdate);
        updateProperties.setProperty(UpdatePropertiesAction.TARGET_ID_KEY, PROFILE_TEST_ID);
        updateProperties.setProperty(UpdatePropertiesAction.TARGET_TYPE_KEY, "profile");
        int changes = eventService.send(updateProperties);

        LOGGER.info("Changes of the event : {}", changes);

        profileToUpdate = profileService.load(PROFILE_TEST_ID);
        Assert.assertEquals("UPDATED FIRST NAME", profileToUpdate.getProperty("firstName"));

    }
}
