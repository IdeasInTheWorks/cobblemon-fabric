Hello!  
This is a script for checking asset implementation.  

There are a few things needed in this folder to get this running:
- Create a text document called `assets_directory.txt` and paste in the path to where you have cloned the asset repository, for example `D:\Dev\Cobblemon\Repository\cobblemon-assets`
- Create a text document called `sheet_id.txt` and paste in our spreadsheet's identifier, it's found in the link between `../d/` and `/edit..`

Both text files and are ignored by git and will not get pushed.

If you don't have Python already, you will need to install it from here: https://www.python.org/downloads/

Then you will need external libraries, open the terminal (search for `cmd` on Windows) and type in the following: `pip install pandas soundfile`  
If the terminal says "Requirement already satisfied," but the script doesn't work, try this instead:
- Type in `py -0p`, this will show installed versions and highlight your default with `*`
- Type in `py -3.14 -m pip install pandas soundfile`, replace `-3.14` with your default version

If there are any issues, or you need help setting your folder up, poke Sterr on Discord.