package project.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import project.dao.HabitDAO;
import project.model.Habit;
import project.util.LoggerUtil;

public class GoogleCalendarSyncService {
    private static final String APPLICATION_NAME = "Habit Tracker Sync";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_EVENTS);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final Logger logger = LoggerUtil.getLogger(GoogleCalendarSyncService.class);
    private final HabitDAO habitDAO = new HabitDAO();
    
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        InputStream in = GoogleCalendarSyncService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new IOException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
    
    public void syncCompletedHabits() {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            List<Habit> completedHabits = habitDAO.getAllHabits();
            for (Habit habit : completedHabits) {
                if (habit.isCompleted()) {
                    Event event = new Event()
                            .setSummary("Completed Habit: " + habit.getName())
                            .setDescription("This habit was marked as completed on " + habit.getCreatedDate());

                    Date eventDate = Date.from(habit.getCreatedDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
                    EventDateTime eventDateTime = new EventDateTime()
                            .setDate(new com.google.api.client.util.DateTime(eventDate));
                    event.setStart(eventDateTime);
                    event.setEnd(eventDateTime);

                    service.events().insert("primary", event).execute();
                    logger.info("Synced habit to Google Calendar: " + habit.getName());
                }
            }
        } catch (IOException | GeneralSecurityException e) {
            logger.error("Failed to sync habits with Google Calendar: " + e.getMessage(), e);
        }
    }
}
