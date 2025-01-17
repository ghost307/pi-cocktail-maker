package net.alex9849.cocktailmaker.config.websocket;

import net.alex9849.cocktailmaker.model.user.ERole;
import net.alex9849.cocktailmaker.service.WebSocketService;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new WebSocketTopicSubscriptionInterceptor(WebSocketService.WS_ACTIONS_STATUS_DESTINATION, ERole.ROLE_ADMIN));
        registration.interceptors(new WebSocketTopicSubscriptionInterceptor(WebSocketService.WS_ACTIONS_LOG_DESTINATION, ERole.ROLE_ADMIN));
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/websocket").setAllowedOriginPatterns("*");
        registry.addEndpoint("/websocket").setAllowedOriginPatterns("*").withSockJS()
        .setClientLibraryUrl("https://cdn.jsdelivr.net/npm/sockjs-client@1.5.0/dist/sockjs.min.js");
    }

}
