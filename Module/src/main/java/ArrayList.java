public class ArrayList <T> {
    T[] array;
    int size;

    public ArrayList(T[] array, int size) {
        this.array = array;
        this.size = size;
    }

    public boolean equals(Object other) {
        switch (other) {
            case ArrayList array_other:
                if ((this.size != array_other.size) || (this.array.length != array_other.array.length)){
                    return false;
                }
                for (int i = 0; i < this.array.length; i += 1) {
                    if (this.array[i] != array_other.array[i]) {
                        return false;
                    }
                }
                return true;
            case null, default:
                return false;}
    }
}