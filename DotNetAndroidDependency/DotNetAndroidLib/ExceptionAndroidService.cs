using Android.Runtime;

namespace DotNetAndroidLib;

[Register("com/roydammarell/dotnetandroid/ExceptionAndroidService")]
public class ExceptionAndroidService : Java.Lang.Object
{
    [Register("throwNullReferenceException", "()V", "")]
    public virtual void ThrowNullReferenceException()
    {
        throw new NullReferenceException("Testing C# NullReferenceException from .NET 9");
    }
}
