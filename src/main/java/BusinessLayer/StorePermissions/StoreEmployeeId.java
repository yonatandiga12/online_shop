package BusinessLayer.StorePermissions;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class StoreEmployeeId implements Serializable {
    private Integer user;
    private Integer store;

    public StoreEmployeeId(Integer user, Integer store) {
        this.user = user;
        this.store = store;
    }

    public StoreEmployeeId() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoreEmployeeId that = (StoreEmployeeId) o;
        return user == that.user && store == that.store;
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, store);
    }

    public Integer getUser() {
        return user;
    }

    public void setUser(Integer user) {
        this.user = user;
    }

    public Integer getStore() {
        return store;
    }

    public void setStore(Integer store) {
        this.store = store;
    }
}
