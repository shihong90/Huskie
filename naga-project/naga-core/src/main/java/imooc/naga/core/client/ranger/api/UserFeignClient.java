package imooc.naga.core.client.ranger.api;

import feign.Param;
import feign.RequestLine;
import imooc.naga.core.client.ranger.model.User;
import imooc.naga.core.client.ranger.util.RangerClientException;

public interface UserFeignClient {

  /*
  Mapper to Ranger User APIs
   */
  @RequestLine("POST /service/xusers/secure/users")
  User createUser(final User user) throws RangerClientException;

  @RequestLine("DELETE /service/xusers/secure/users/id/{id}?forceDelete={forceDelete}")
  void deleteUser(@Param("id") Integer id, @Param("forceDelete") boolean forceDelete);

  @RequestLine("GET /service/xusers/users/userName/{name}")
  User getUserByName(@Param("name") String name) throws RangerClientException;

}
