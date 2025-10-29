using Android.Runtime;
using Java.Interop;
using System.Diagnostics.CodeAnalysis;
using HandlebarsDotNet;

namespace DotNetAndroidLib;

[Register("com/roydammarell/dotnetandroid/HelloAndroidService")]
public class HelloAndroidService : Java.Lang.Object
{
    [Register("createHello", "()Ljava/lang/String;", "")]
    [Export("createHello")]
    [UnconditionalSuppressMessage("Trimming", "IL2026", Justification = "Exported for Android Callable Wrapper binding.")]
    public virtual string CreateHello()
    {
        var template = Handlebars.Compile("🎉 Handlebars says: Hello from {{runtime}} on {{platform}}!");
        var data = new
        {
            runtime = ".NET 9",
            platform = "Android"
        };
        return template(data);
    }
}
