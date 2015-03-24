/*
 * Copyright 2012 - 2014 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinymediamanager.core.tvshow;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.apache.commons.io.FileUtils;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.ITmmModule;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The class TvShowModuleManager. Used to manage the tv show module
 * 
 * @author Manuel Laggner
 */
public class TvShowModuleManager implements ITmmModule {
  private static final String        MODULE_TITLE = "TV show management";
  private static final String        TV_SHOW_DB   = "tvshows";
  private static TvShowModuleManager instance;

  private boolean                    enabled;

  private ObjectMapper               objectMapper;
  private Connection                 connection;

  private TvShowModuleManager() {
    enabled = false;
  }

  public static TvShowModuleManager getInstance() {
    if (instance == null) {
      instance = new TvShowModuleManager();
    }
    return instance;
  }

  @Override
  public String getModuleTitle() {
    return MODULE_TITLE;
  }

  @Override
  public void startUp() throws Exception {
    // start and configure h2
    Class.forName("org.h2.Driver");
    connection = DriverManager.getConnection("jdbc:h2:./" + TV_SHOW_DB + ";compress=true;defrag_always=true;RETENTION_TIME=0", "", "");

    Statement stat = connection.createStatement();
    stat.execute("create table if not exists tv_show(id uuid primary key, data clob)");
    stat.execute("create table if not exists episode(id uuid primary key, data clob)");
    connection.setAutoCommit(true);

    // configure JSON
    objectMapper = new ObjectMapper();
    objectMapper.configure(MapperFeature.AUTO_DETECT_GETTERS, false);
    objectMapper.configure(MapperFeature.AUTO_DETECT_IS_GETTERS, false);
    objectMapper.configure(MapperFeature.AUTO_DETECT_SETTERS, false);
    objectMapper.configure(MapperFeature.AUTO_DETECT_FIELDS, false);
    objectMapper.setSerializationInclusion(Include.NON_NULL);
    objectMapper.setSerializationInclusion(Include.NON_EMPTY);
    objectMapper.setSerializationInclusion(Include.NON_DEFAULT);

    TvShowList.getInstance().loadTvShowsFromDatabase();
    enabled = true;
  }

  @Override
  public void shutDown() throws Exception {
    connection.commit();
    connection.close();
    enabled = false;

    if (Globals.settings.isDeleteTrashOnExit()) {
      for (String ds : Globals.settings.getTvShowSettings().getTvShowDataSource()) {
        File file = new File(ds + File.separator + Constants.BACKUP_FOLDER);
        FileUtils.deleteQuietly(file);
      }
    }
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  Connection getConnection() {
    return connection;
  }

  ObjectMapper getObjectMapper() {
    return objectMapper;
  }
}
