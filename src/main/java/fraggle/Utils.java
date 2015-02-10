package fraggle;

public class Utils
{
    static boolean nodeOverlap(PosSize lhs, PosSize rhs) {
        PosSize mn = lhs.pos.x < rhs.pos.x ? lhs : rhs;
        PosSize mx = lhs.pos.x < rhs.pos.x ? rhs : lhs;
        if (mn.pos.x <= mx.pos.x && mn.pos.x + mn.size.x > mx.pos.x) {

            mn = lhs.pos.y < rhs.pos.y ? lhs : rhs;
            mx = lhs.pos.y < rhs.pos.y ? rhs : lhs;
            return mn.pos.y <= mx.pos.y && mn.pos.y + mn.size.y > mx.pos.y;
        }
        return false;
    }

}
