using System.Runtime.InteropServices;
using System.Text;
using HandlebarsDotNet;

namespace DotNetAndroidLib;

public static class HelloAndroidService
{
    [UnmanagedCallersOnly(EntryPoint = "create_hello")]
    public static unsafe IntPtr CreateHello()
    {
        try
        {
            var template = Handlebars.Compile("🎉 Handlebars says: Hello from {{runtime}} on {{platform}}!");
            var data = new
            {
                runtime = ".NET 9 NativeAOT",
                platform = "Android"
            };
            string result = template(data);
            byte[] utf8Bytes = Encoding.UTF8.GetBytes(result + '\0');
            IntPtr buffer = Marshal.AllocHGlobal(utf8Bytes.Length);
            Marshal.Copy(utf8Bytes, 0, buffer, utf8Bytes.Length);
            return buffer;
        }
        catch
        {
            return IntPtr.Zero;
        }
    }

    [UnmanagedCallersOnly(EntryPoint = "free_string")]
    public static unsafe void FreeString(IntPtr ptr)
    {
        if (ptr != IntPtr.Zero)
        {
            Marshal.FreeHGlobal(ptr);
        }
    }
}
