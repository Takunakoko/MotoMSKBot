import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.wall.responses.GetResponse;

class VkApi {
    private VkApiClient vk;
    private UserActor actor;

    void initApi(){
        TransportClient transportClient = HttpTransportClient.getInstance();
         vk = new VkApiClient(transportClient);
         actor = new UserActor(Constants.USER_ID, Constants.ACCESS_TOKEN);
    }

    GetResponse getResponseFromWall() throws Exception{
        return vk.wall().get(actor)
                .ownerId(Constants.OWNER_ID)
                .count(15)
                .execute();
    }


}
