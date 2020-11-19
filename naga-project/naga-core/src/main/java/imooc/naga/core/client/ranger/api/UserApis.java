package imooc.naga.core.client.ranger.api;

import feign.Param;
import imooc.naga.core.client.ranger.model.User;
import imooc.naga.core.client.ranger.util.RangerClientException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class UserApis {

  private final UserFeignClient client;

  public User createUser(final User user) throws RangerClientException {
    return client.createUser(user);
  }

  public void deleteUser(@Param("id") Integer id,@Param("forceDelete") boolean forceDelete) {
    client.deleteUser(id,forceDelete);
  }

  public User getUserByName(@Param("name") String name) throws RangerClientException {
    return client.getUserByName(name);
  }
}