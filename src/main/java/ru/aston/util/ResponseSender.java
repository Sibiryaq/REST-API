package ru.aston.util;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

import static ru.aston.util.Constants.APPLICATION_JSON;
import static ru.aston.util.Constants.CHARACTER_ENCODING;

@Slf4j
public class ResponseSender {

    public void sendResponse(HttpServletResponse resp, Integer status, Object body) {
        resp.setCharacterEncoding(CHARACTER_ENCODING);
        resp.setContentType(APPLICATION_JSON);
        resp.setStatus(status);
        Writer writer;
        try {
            writer = resp.getWriter();
            writer.write(GetProvider.getObjectMapper().writeValueAsString(body));
            log.info("Send response with status : " + status + " , body : " + body);
        } catch (IOException e) {
            log.warn(e.getMessage());
        }
    }

}
