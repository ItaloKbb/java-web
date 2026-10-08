package br.senai.aula.web.infrastructure.web.chat;
import br.senai.aula.web.application.auth.AuthenticationService;
import br.senai.aula.web.application.chat.ChatService;
import br.senai.aula.web.infrastructure.persistence.chat.entity.ChatMessageJpaEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;
@RestController @RequestMapping("/chat")
public class ChatController {
 private final AuthenticationService authentication; private final ChatService chat;
 public ChatController(AuthenticationService authentication,ChatService chat){this.authentication=authentication;this.chat=chat;}
 public record SendMessageRequest(@NotBlank(message="Digite uma mensagem") @Size(max=ChatMessageJpaEntity.MAX_LENGTH,message="Mensagem muito longa") String text){}
 public record ChatMessageResponse(Long id,Long authorId,String nickname,String text,Instant sentAt){
  static ChatMessageResponse from(ChatMessageJpaEntity m){return new ChatMessageResponse(m.getId(),m.getUser().getId(),m.getUser().getNickname(),m.getText(),m.getSentAt());}
 }
 @GetMapping("/messages") public List<ChatMessageResponse> list(@RequestHeader("X-Player-Token") String token,@RequestParam(required=false) Long after){authentication.requireUser(token);return chat.list(after).stream().map(ChatMessageResponse::from).toList();}
 @PostMapping("/messages") @ResponseStatus(HttpStatus.CREATED) public ChatMessageResponse send(@RequestHeader("X-Player-Token") String token,@Valid @RequestBody SendMessageRequest r){return ChatMessageResponse.from(chat.send(authentication.requireUser(token),r.text()));}
}
