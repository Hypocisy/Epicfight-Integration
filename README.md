Please download this mod and place it in the mods folder, then start the game. Make sure you have installed Epicfight and PMMO.

Input the command: `/efi genSkillData`

In your `/saves/your_save/datapacks/efi_compat_pack/data/efi_mod/skill_settings` folder, there are two subfolders named `learn_able_skills` and `other_skills`. The difference is that one restricts the learning of Epic Fight skill books through the GUI interface, while the other restricts the use of other types of skills.

The file name with the .json extension is the name of your Epic Fight skill. Open any random file, for example, `parrying.json` in the `learn_able_skills` folder:

```json
{
  "templates": [],
  "default_requirements": {
    "level": {
      "building": 20,
      "combat": 30,
      "farming": 50
    }
  },
  "override": false
}
After setting it up this way, PMMO2's building level must be greater than 20, combat level greater than 30, and farming level greater than 50 to learn parrying. If you have already learned parrying, but still do not meet the above conditions, it will still prevent you from using the skill. The logic is similar for other passive skills.

Remember to re-enter the save or restart the server to make your changes take effect.